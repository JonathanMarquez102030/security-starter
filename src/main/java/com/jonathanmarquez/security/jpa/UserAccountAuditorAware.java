package com.jonathanmarquez.security.jpa;

import com.jonathanmarquez.security.security.model.SecurityUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementación de AuditorAware que proporciona la ID del usuario autenticado actual para la auditoría JPA.
 * Esta clase se utiliza para completar automáticamente los campos creado_por y modificado_por en entidades auditadas.
 */
@Component
public class UserAccountAuditorAware implements AuditorAware<String> {

  /**
   * Obtiene la identificación del usuario actualmente autenticado del SecurityContext.
   *
   * @return Opcional que contiene el UUID del usuario actual, o vacío si:
   * - No existe autenticación
   * - El usuario no está autenticado
   * - La autenticación es anónima
   * - Principal no es una instancia de SecurityUserDetails
   */
  @Override
  public Optional<String> getCurrentAuditor() {
    return Optional.ofNullable(SecurityContextHolder.getContext())
                   .map(SecurityContext::getAuthentication)
                   .filter(Authentication::isAuthenticated)
                   .filter(auth -> !(auth instanceof AnonymousAuthenticationToken))
                   .map(Authentication::getPrincipal)
                   .filter(principal -> principal instanceof SecurityUserDetails)
                   .map(principal -> ((SecurityUserDetails) principal).getUsername());
  }
}
