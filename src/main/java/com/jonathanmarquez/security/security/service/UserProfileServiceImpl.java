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
   * @param email email del usuario
   * @param rawPassword contraseña en texto plano (se codificará)
   * @param roles lista de authorities/roles a asignar (ej: ["ROLE_USER"])
   * @return el perfil creado
   */
  @Override
  @Transactional
  public UserProfile createUser(String email, String rawPassword, List<Role> roles) {

    // 1. Crea usuario en tabla 'users' de Spring Security
    String encodedPassword = passwordEncoder.encode(rawPassword);
    jdbcTemplate.update(
        "INSERT INTO users (username, password, enabled) VALUES (?, ?, ?)",
        email, encodedPassword, true
    );

    // 2. Asigna authorities según el modo configurado
    if (securityProperties.getAuthorizationMode() == AuthorizationMode.GROUPS) {
      // Modo GRUPOS: Asignar al grupo correspondiente
      for (Role role : roles) {
        String groupName = role.getGroupName();

        // Verifica si el grupo existe
        List<Integer> results = jdbcTemplate.query(
            "SELECT id FROM groups WHERE group_name = ?",
            (rs, rowNum) -> rs.getInt("id"),
            groupName
        );
        Integer groupId = results.isEmpty() ? null : results.getFirst();

        if (groupId == null) {
          // Crea el grupo si no existe
          jdbcTemplate.update(
              "INSERT INTO groups (group_name) VALUES (?)",
              groupName
          );

          // Obtiene el ID del grupo recién creado
          groupId = jdbcTemplate.queryForObject(
              "SELECT id FROM groups WHERE group_name = ?",
              Integer.class,
              groupName
          );

          // Inserta las autoridades del grupo
          jdbcTemplate.update(
              "INSERT INTO group_authorities (group_id, authority) VALUES (?, ?)",
              groupId, role.name()
          );
        }

        // Asigna usuario al grupo
        jdbcTemplate.update(
            "INSERT INTO group_members (username, group_id) VALUES (?, ?)",
            email, groupId
        );
      }
    } else {
      // Modo AUTHORITIES: Inserta directamente en la tabla authorities
      for (Role role : roles) {
        jdbcTemplate.update(
            "INSERT INTO authorities (username, authority) VALUES (?, ?)",
            email, role.name()
        );
      }
    }

    // 3. Crea perfil extendido
    UserProfile profile = UserProfile.builder()
                                     .email(email)
                                     .build();

    return userProfileRepository.save(profile);
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
}