/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.config;

import com.jonathanmarquezperez.email_verification.exception.EmailNotVerifiedException;
import com.jonathanmarquezperez.security.enums.AuthorizationMode;
import com.jonathanmarquezperez.security.model.SecurityUserDetails;
import com.jonathanmarquezperez.security.model.UserProfile;
import com.jonathanmarquezperez.security.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/**
 * Servicio personalizado que combina:
 * 1. Datos de autenticación desde las tablas de Spring Security (users, authorities/groups)
 * 2. Datos de perfil extendidos desde user_profiles (JPA)
 */
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final JdbcTemplate jdbcTemplate;
  private final UserProfileRepository userProfileRepository;
  private final SecurityProperties securityProperties;

  @Override
  public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {

    log.debug("Cargando usuario desde BD: {}", username);

    // 1. Cargar usuario base desde 'users'
    String userSql = "SELECT username, password, enabled FROM users WHERE username = ?";

    var userOpt = jdbcTemplate.query(userSql, (rs, rowNum) ->
        new Object[]{
            rs.getString("username"),
            rs.getString("password"),
            rs.getBoolean("enabled")
        }, username
    ).stream().findFirst();

    if (userOpt.isEmpty()) {
      throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }

    log.debug("Usuario encontrado en BD: {} ", username);

    Object[] userData = userOpt.get();
    String dbUsername = (String) userData[0];
    String password = (String) userData[1];
    boolean enabled = (Boolean) userData[2];

    // 2. Cargar autoridades según el modo configurado
    List<String> authorities = loadAuthorities(dbUsername);

    log.debug("Usuario encontrado en BD: {} (enabled={})", dbUsername, enabled);
    log.debug("Authorities cargadas para {}: {}", dbUsername, authorities);

    // 3. Cargar perfil extendido (opcional)
    UserProfile profile = userProfileRepository.findById(dbUsername).orElse(null);

    if (!enabled && profile != null && !profile.isEmailVerified()) {
      log.warn("Intento de login con email no verificado: {}", username);
      throw new EmailNotVerifiedException();
    }

    // 4. Construir SecurityUser
    return new SecurityUserDetails(dbUsername, password, enabled, authorities, profile);
  }

  /**
   * Carga las autoridades según el modo configurado (AUTHORITIES o GROUPS).
   */
  private List<String> loadAuthorities(String username) {

    if (securityProperties.getAuthorizationMode() == AuthorizationMode.GROUPS) {
      log.debug("Cargando authorities por GRUPOS para: {}", username);
      // Consulta para obtener autoridades desde grupos
      String groupSql = """
              SELECT ga.authority
              FROM group_authorities ga
              INNER JOIN group_members gm ON ga.group_id = gm.group_id
              WHERE gm.username = ?
          """;
      return jdbcTemplate.queryForList(groupSql, String.class, username);

    } else {
      log.debug("Cargando authorities DIRECTAS para: {}", username);

      // Consulta para obtener autoridades directas
      String authSql = "SELECT authority FROM authorities WHERE username = ?";
      return jdbcTemplate.queryForList(authSql, String.class, username);
    }
  }
}
