/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.security.config;

import com.jonathanmarquez.security.security.enums.AuthorizationMode;
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
    private List<String> allowedOrigins  = new ArrayList<>();
    private List<String> allowedMethods  = List.of("*");
    private List<String> allowedHeaders  = List.of("*");
    private List<String> exposedHeaders  = List.of("Authorization", "X-XSRF-TOKEN");
    private boolean      allowCredentials = true;
    private long         maxAge           = 3600L;
  }

  @Getter
  @Setter
  public static class Csrf {
    /** Rutas excluidas de la validación CSRF. */
    private List<String> ignoredPaths = new ArrayList<>(List.of(
            "/auth/register", "/auth/csrf",  "/auth/refresh",
            "/auth/public/**", "/auth/login", "/auth/logout",
            "/auth/verify",    "/auth/resend-otp", "/auth/password/**"
    ));
  }

  @Getter
  @Setter
  public static class Cookie {
    /** true = cookies solo viajan por HTTPS. Activa en producción. */
    private boolean secure   = false;
    private String  sameSite = "Lax";
  }

  @Getter
  @Setter
  public static class Authorization {
    /** Rutas accesibles sin autenticación. */
    private List<String> publicPaths = new ArrayList<>(List.of(
            "/auth/register", "/auth/csrf",  "/auth/refresh",
            "/auth/public/**", "/auth/verify", "/auth/resend-otp",
            "/auth/password/**", "/error",    "/test/**"
    ));
  }
}