package com.jonathanmarquez.security.security.config;

import com.jonathanmarquez.security.security.enums.AuthorizationMode;
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
   * Determina si los grupos están habilitados basándose en el modo de autorización.
   *
   * @return true si el modo es GROUPS, false en caso contrario
   */
  public boolean isEnableGroups() {
    return authorizationMode == AuthorizationMode.GROUPS;
  }
}