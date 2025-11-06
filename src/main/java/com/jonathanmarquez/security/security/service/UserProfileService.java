package com.jonathanmarquez.security.security.service;

import com.jonathanmarquez.security.security.model.UserProfile;
import com.jonathanmarquez.security.security.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class UserProfileService {

  private final JdbcTemplate jdbcTemplate;
  private final UserProfileRepository userProfileRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Crea un usuario completo: credenciales + perfil extendido.
   *
   * @param email email del usuario
   * @param rawPassword contraseña en texto plano (se encodificará)
   * @param authorities lista de authorities a asignar (ej: ["ROLE_USER"])
   * @return el perfil creado
   */
  @Transactional
  public UserProfile createUser(String email, String rawPassword, List<String> authorities) {

    // 1. Crear usuario en tabla 'users' de Spring Security
    String encodedPassword = passwordEncoder.encode(rawPassword);
    jdbcTemplate.update(
        "INSERT INTO users (username, password, enabled) VALUES (?, ?, ?)",
        email, encodedPassword, true
    );

    // 2. Asignar authorities
    for (String authority : authorities) {
      jdbcTemplate.update(
          "INSERT INTO authorities (username, authority) VALUES (?, ?)",
          email, authority
      );
    }

    // 3. Crear perfil extendido
    UserProfile profile = UserProfile.builder()
                                     .email(email)
                                     .build();

    return userProfileRepository.save(profile);
  }

  /**
   * Actualiza solo el perfil extendido (no afecta credenciales).
   */
  public UserProfile updateProfile(UserProfile profile) {
    return userProfileRepository.save(profile);
  }

  //TODO: Implementar con mejor seguridad, con validación de correo electrónico. y con buenas practicas de seguridad.
  /**
   * Actualiza la contraseña del usuario.
   */
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
  public boolean userExists(String email) {
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM users WHERE username = ?",
        Integer.class,
        email
    );
    return count != null && count > 0;
  }
}