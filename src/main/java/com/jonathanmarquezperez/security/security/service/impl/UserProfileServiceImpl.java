/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.security.service.impl;

import com.jonathanmarquezperez.security.email_verification.service.OtpService;
import com.jonathanmarquezperez.security.exceptions.customexceptions.EmailAddressAlreadyExistsException;
import com.jonathanmarquezperez.security.security.config.SecurityProperties;
import com.jonathanmarquezperez.security.security.enums.AuthorizationMode;
import com.jonathanmarquezperez.security.security.enums.Role;
import com.jonathanmarquezperez.security.security.model.UserProfile;
import com.jonathanmarquezperez.security.security.model.dto.RegisterRequestDto;
import com.jonathanmarquezperez.security.security.model.dto.UserProfileDto;
import com.jonathanmarquezperez.security.security.model.mapper.UserProfileMapper;
import com.jonathanmarquezperez.security.security.repository.UserProfileRepository;
import com.jonathanmarquezperez.security.security.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar usuarios completos:
 * - Crea usuario en 'users' (Spring Security)
 * - Asigna authorities
 * - Crea perfil extendido en 'user_profiles' (JPA)
 */
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

  private final JdbcTemplate jdbcTemplate;
  private final UserProfileRepository userProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService;
  private final SecurityProperties securityProperties;
  private final UserProfileMapper userProfileMapper;
  private final OtpService otpService;

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
   * @param registerRequestDto datos del usuario a crear
   * @param roles              lista de authorities/roles a asignar (ej: ["ROLE_USER"])
   * @return el perfil creado
   */
  @Override
  @Transactional
  public UserProfile createUser(RegisterRequestDto registerRequestDto, List<Role> roles) {

    if (userExists(registerRequestDto.email())) {
      throw new EmailAddressAlreadyExistsException(
          String.format("El email '%s' ya está registrado", registerRequestDto.email())
      );
    }

    createUserCredentials(registerRequestDto.email(), registerRequestDto.password());
    assignAuthorities(registerRequestDto.email(), roles);

    UserProfile profile = createUserProfile(registerRequestDto.profile());

    log.info("Usuario creado exitosamente (pendiente de verificación): {}", registerRequestDto.email());

    return profile;
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


  /**
   * Obtiene el hash de la contraseña actual desde la tabla users.
   * Devuelve null si el usuario no existe o no tiene password.
   */
  @Override
  public String getPasswordHash(String email) {
    try {
      return jdbcTemplate.queryForObject(
          "SELECT password FROM users WHERE username = ?",
          String.class,
          email
      );
    } catch (EmptyResultDataAccessException ex) {
      return null;
    }
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
        "INSERT INTO users (username, password, enabled) VALUES (?, ?, false)",
        email, encodedPassword
    );
    log.debug("Credenciales creadas para usuario: {} (enabled=false)", email);
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
  private UserProfile createUserProfile(UserProfileDto userProfileDto) {
    UserProfile profile = userProfileMapper.toUserProfile(userProfileDto);
    profile.setEmailVerified(false);
    return userProfileRepository.save(profile);
  }

  /**
   * Habilita o deshabilita un usuario.
   * MODIFICADO: Eliminar OTP al habilitar
   */
  @Override
  @Transactional
  public void setEnabled(String email, boolean enabled) {
    String sql = "UPDATE users SET enabled = ? WHERE username = ?";
    int updated = jdbcTemplate.update(sql, enabled, email);

    if (updated == 0) {
      throw new UsernameNotFoundException("Usuario no encontrado: " + email);
    }

    log.info("Usuario {} {}", email, enabled ? "habilitado" : "deshabilitado");
  }

  /**
   * establece un email verificado o no
   */
  @Transactional
  public void setEmailVerified(String email, boolean verified) {
    setEnabled(email, verified);

    Optional<UserProfile> userProfile = userProfileRepository.findByEmail(email);

    userProfile.ifPresentOrElse(
        profile -> {
          profile.setEmailVerified(verified);
          userProfileRepository.save(profile);

          if (verified) {
            try {
              otpService.deleteAllOtpsByEmail(email);
              log.info("Tokens OTP eliminados para usuario: {}", email);
            } catch (Exception e) {
              log.error("Error al eliminar OTP para {}: {}", email, e.getMessage());
            }
          }
        },
        () -> {
          throw new UsernameNotFoundException("Usuario no encontrado: " + email);
        }
    );
  }
}
