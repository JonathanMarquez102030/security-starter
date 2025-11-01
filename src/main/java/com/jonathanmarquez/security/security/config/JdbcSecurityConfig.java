
package com.jonathanmarquez.security.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;

/**
 * Configuración de JDBCUserDetailsManager para trabajar con las tablas
 * estándar de Spring Security.
 *
 * SOLO se activa si security.user-details-service-type=JDBC
 */
@Configuration
@RequiredArgsConstructor
public class JdbcSecurityConfig {

  private final DataSource dataSource;
  private final SecurityProperties securityProperties;

  /**
   * Bean de JDBCUserDetailsManager configurado según el modo de autorización.
   *
   * Este bean SOLO se crea si:
   * - security.user-details-service-type=JDBC en application.yml
   *
   * Casos de uso:
   * - Proyecto simple que NO necesita perfil extendido (UserProfile)
   * - Solo requiere las tablas estándar de Spring Security
   */
  @Bean
  public JdbcUserDetailsManager jdbcUserDetailsManager() {
    JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

    // Habilitar o deshabilitar grupos según configuración
    manager.setEnableGroups(securityProperties.isEnableGroups());

    return manager;
  }
}