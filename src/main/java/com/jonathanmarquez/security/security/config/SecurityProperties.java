package com.jonathanmarquez.security.security.config;

import com.jonathanmarquez.security.security.enums.AuthorizationMode;
import com.jonathanmarquez.security.security.enums.UserDetailsServiceType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades configurables para el sistema de seguridad.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * Define si se usa AUTHORITIES o GROUPS para la autorización.
     */
    private AuthorizationMode authorizationMode = AuthorizationMode.AUTHORITIES;

    /**
     * Habilita o deshabilita el uso de grupos en JDBCUserDetailsManager.
     */
    private boolean enableGroups = false;

  /**
   * Define qué implementación de UserDetailsService usar:
   * - JDBC: usa JdbcUserDetailsManager (solo tablas de Spring Security)
   * - CUSTOM: usa CustomUserDetailsService (tablas Spring Security + UserProfile extendido)
   */
  private UserDetailsServiceType userDetailsServiceType = UserDetailsServiceType.CUSTOM;
}