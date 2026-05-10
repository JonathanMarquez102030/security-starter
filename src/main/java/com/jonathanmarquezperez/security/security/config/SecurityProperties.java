/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.security.config;

import com.jonathanmarquezperez.security.security.enums.AuthorizationMode;
import com.jonathanmarquezperez.security.security.enums.RuleType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Propiedades configurables para el sistema de seguridad.
 */
@Getter
@Setter
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

  // ── CORS ─────────────────────────────────────────────────────────────────────
  private final Cors cors = new Cors();

  // ── CSRF ─────────────────────────────────────────────────────────────────────
  private final Csrf csrf = new Csrf();

  // ── Cookie ───────────────────────────────────────────────────────────────────
  private final Cookie cookie = new Cookie();

  // ── Authorization ─────────────────────────────────────────────────────────────
  private final Authorization authorization = new Authorization();

  // ═════════════════════════════════════════════════════════════════════════════

  @Getter
  @Setter
  public static class Cors {
    /**
     * Orígenes permitidos para peticiones CORS.
     */
    private List<String> allowedOrigins = new ArrayList<>();
    /**
     * Métodos HTTP permitidos en peticiones CORS.
     */
    private List<String> allowedMethods = List.of("*");
    /**
     * Encabezados permitidos en peticiones CORS.
     */
    private List<String> allowedHeaders = List.of("*");
    /**
     * Encabezados expuestos al cliente en respuestas CORS.
     */
    private List<String> exposedHeaders = List.of("Authorization", "X-XSRF-TOKEN");
    /**
     * Indica si se permiten credenciales (cookies, autorización) en peticiones CORS.
     */
    private boolean allowCredentials = true;
    /**
     * Tiempo máximo (en segundos) que el navegador cachea la respuesta preflight CORS.
     */
    private long maxAge = 3600L;
  }

  @Getter
  @Setter
  public static class Csrf {
    /** Rutas excluidas de la validación CSRF. */
    private List<String> ignoredPaths = new ArrayList<>();
  }

  @Getter
  @Setter
  public static class Cookie {
    /** true = cookies solo viajan por HTTPS. Activa en producción. */
    private boolean secure   = false;
    /**
     * Política SameSite para cookies (Strict, Lax, None). Protege contra ataques CSRF.
     */
    private String  sameSite = "Lax";
  }

  @Getter
  @Setter
  public static class Authorization {
    /** Rutas accesibles sin autenticación. */
    private List<String> publicPaths = new ArrayList<>();

    /**
     * Rutas accesibles solo para usuarios autenticados.
     */
    private List<String> authenticatedPaths = new ArrayList<>();

    /**
     * Reglas de autorización basadas en roles/authorities definidas por el consumidor.
     * Se evalúan en el orden en que están declaradas, antes del catch-all anyRequest().
     */
    private List<AuthorizationRule> rules = new ArrayList<>();
  }

  /**
   * Define una regla de autorización para un conjunto de rutas.
   * <p>
   * Ejemplo en application.yml:
   * <pre>
   * security:
   *   authorization:
   *     rules:
   *       - paths: ["/admin/**"]
   *         type: HAS_ROLE
   *         roles: ["ADMIN"]
   *       - paths: ["/reports/**"]
   *         type: HAS_ANY_ROLE
   *         roles: ["ADMIN", "ANALYST"]
   *       - paths: ["/super/**"]
   *         type: HAS_ALL_ROLES
   *         roles: ["ADMIN", "SUPERUSER"]
   *       - paths: ["/api/read/**"]
   *         type: HAS_ANY_AUTHORITY
   *         authorities: ["READ_DATA", "READ_ALL"]
   * </pre>
   */
  @Getter
  @Setter
  public static class AuthorizationRule {

    /**
     * Rutas a las que aplica esta regla. Soporta patrones Ant (**, *, ?).
     */
    private List<String> paths = new ArrayList<>();

    /**
     * Tipo de verificación a aplicar.
     */
    private RuleType type = RuleType.AUTHENTICATED;

    /**
     * Roles requeridos. Se usan con HAS_ROLE, HAS_ANY_ROLE, HAS_ALL_ROLES.
     * Spring Security agrega el prefijo ROLE_ automáticamente.
     */
    private List<String> roles = new ArrayList<>();

    /**
     * Authorities requeridas. Se usan con HAS_AUTHORITY, HAS_ANY_AUTHORITY, HAS_ALL_AUTHORITIES.
     * Se comparan exactamente como se declaran (sin prefijo automático).
     */
    private List<String> authorities = new ArrayList<>();
  }
}
