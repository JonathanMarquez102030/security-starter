package com.jonathanmarquez.security.security.service;

import com.jonathanmarquez.security.security.config.SecurityProperties;
import com.jonathanmarquez.security.security.enums.AuthorizationMode;
import com.jonathanmarquez.security.security.enums.Role;
import com.jonathanmarquez.security.security.model.UserProfile;
import com.jonathanmarquez.security.security.repository.UserProfileRepository;
import com.jonathanmarquez.security.security.service.ports.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestionar usuarios completos:
 * - Crea usuario en 'users' (Spring Security)
 * - Asigna authorities
 * - Crea perfil extendido en 'user_profiles' (JPA)
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final JdbcTemplate jdbcTemplate;
  private final UserProfileRepository userProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService;
  private final SecurityProperties securityProperties;

  /**
   * Crea un usuario completo: credenciales + perfil extendido.
   *
   * <p>
   * Esta función realiza tres operaciones principales:
   * 1. Crea el usuario en la tabla 'users' de Spring Security
   * 2. Asigna authorities según el modo configurado (GROUPS o AUTHORITIES)
   * 3. Crea el perfil extendido en 'user_profiles'
   * </p>
   *
   * @param email email del usuario
   * @param rawPassword contraseña en texto plano (se codificará)
   * @param roles lista de authorities/roles a asignar (ej: ["ROLE_USER"])
   * @return el perfil creado
   */
  @Override
  @Transactional
  public UserProfile createUser(String email, String rawPassword, List<Role> roles) {
    createUserCredentials(email, rawPassword);
    assignAuthorities(email, roles);
    return createUserProfile(email);
  }

  @Override
  public UserDetails getUserDetails(String email) {
    return userDetailsService.loadUserByUsername(email);
  }

  /**
   * Actualiza solo el perfil extendido (no afecta credenciales).
   */
  @Override
  @Transactional
  public UserProfile updateProfile(UserProfile profile) {
    return userProfileRepository.save(profile);
  }

  //TODO: Implementar con mejor seguridad, con validación de correo electrónico. y con buenas practicas de seguridad.
  /**
   * Actualiza la contraseña del usuario.
   */
  @Override
  @Transactional
  public void updatePassword(String email, String newRawPassword) {
    String encodedPassword = passwordEncoder.encode(newRawPassword);
    jdbcTemplate.update(
        "UPDATE users SET password = ? WHERE username = ?",
        encodedPassword, email
    );
  }

  //TODO: Implementar cuando este mas estandarizado y con buenas practicas de seguridad para confirmar.
  /**
   * Elimina completamente un usuario (con cascada a perfil y authorities).
   */
  @Override
  @Transactional
  public void deleteUser(String email) {
    // 1. Eliminar authorities
    jdbcTemplate.update("DELETE FROM authorities WHERE username = ?", email);

    // 2. Eliminar usuario (cascada elimina perfil por FK ON DELETE CASCADE)
    jdbcTemplate.update("DELETE FROM users WHERE username = ?", email);
  }

  //TODO: implementar para desactivación o activación de usuarios, verificar usos practicos.
  /**
   * Habilita o deshabilita un usuario.
   */
  @Override
  @Transactional
  public void setEnabled(String email, boolean enabled) {
    jdbcTemplate.update(
        "UPDATE users SET enabled = ? WHERE username = ?",
        enabled, email
    );
  }

  /**
   * Verifica si existe un usuario.
   */
  @Override
  public boolean userExists(String email) {
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM users WHERE username = ?",
        Integer.class,
        email
    );
    return count != null && count > 0;
  }


  /**
   * Crea las credenciales del usuario en la tabla 'users' de Spring Security.
   */
  private void createUserCredentials(String email, String rawPassword) {
    String encodedPassword = passwordEncoder.encode(rawPassword);
    jdbcTemplate.update(
        "INSERT INTO users (username, password, enabled) VALUES (?, ?, ?)",
        email, encodedPassword, true
    );
  }

  /**
   * Asigna authorities al usuario según el modo configurado.
   */
  private void assignAuthorities(String email, List<Role> roles) {
    if (securityProperties.getAuthorizationMode() == AuthorizationMode.GROUPS) {
      assignAuthoritiesViaGroups(email, roles);
    } else {
      assignAuthoritiesDirectly(email, roles);
    }
  }

  /**
   * Asigna authorities mediante grupos.
   */
  private void assignAuthoritiesViaGroups(String email, List<Role> roles) {
    for (Role role : roles) {
      String groupName = role.getGroupName();
      Integer groupId = getOrCreateGroup(groupName, role);
      assignUserToGroup(email, groupId);
    }
  }

  /**
   * Obtiene o crea un grupo si no existe.
   */
  private Integer getOrCreateGroup(String groupName, Role role) {
    List<Integer> results = jdbcTemplate.query(
        "SELECT id FROM groups WHERE group_name = ?",
        (rs, rowNum) -> rs.getInt("id"),
        groupName
    );

    Integer groupId = results.isEmpty() ? null : results.getFirst();

    if (groupId == null) {
      createGroup(groupName);
      groupId = getGroupId(groupName);
      createGroupAuthority(groupId, role);
    }

    return groupId;
  }

  /**
   * Crea un nuevo grupo.
   */
  private void createGroup(String groupName) {
    jdbcTemplate.update(
        "INSERT INTO groups (group_name) VALUES (?)",
        groupName
    );
  }

  /**
   * Obtiene el ID de un grupo por su nombre.
   */
  private Integer getGroupId(String groupName) {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM groups WHERE group_name = ?",
        Integer.class,
        groupName
    );
  }

  /**
   * Crea la autoridad asociada a un grupo.
   */
  private void createGroupAuthority(Integer groupId, Role role) {
    jdbcTemplate.update(
        "INSERT INTO group_authorities (group_id, authority) VALUES (?, ?)",
        groupId, role.name()
    );
  }

  /**
   * Asigna un usuario a un grupo.
   */
  private void assignUserToGroup(String email, Integer groupId) {
    jdbcTemplate.update(
        "INSERT INTO group_members (username, group_id) VALUES (?, ?)",
        email, groupId
    );
  }

  /**
   * Asigna authorities directamente sin usar grupos.
   */
  private void assignAuthoritiesDirectly(String email, List<Role> roles) {
    for (Role role : roles) {
      jdbcTemplate.update(
          "INSERT INTO authorities (username, authority) VALUES (?, ?)",
          email, role.name()
      );
    }
  }

  /**
   * Crea el perfil extendido del usuario.
   */
  private UserProfile createUserProfile(String email) {
    UserProfile profile = UserProfile.builder()
        .email(email)
        .build();
    return userProfileRepository.save(profile);
  }
}