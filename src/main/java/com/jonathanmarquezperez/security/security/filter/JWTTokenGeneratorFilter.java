/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.security.filter;

import com.jonathanmarquezperez.security.security.utils.CookieUtil;
import com.jonathanmarquezperez.security.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que genera tokens JWT tras una autenticación exitosa.
 *
 * <p>Este filtro intercepta peticiones al endpoint de login y, si existe un usuario
 * autenticado en el contexto de seguridad, genera un par de tokens JWT (access token
 * y refresh token) que se almacenan en cookies HTTP-only seguras. También envía el
 * access token en el encabezado Authorization para compatibilidad con clientes que
 * prefieran ese mecanismo.</p>
 */
@RequiredArgsConstructor
@Slf4j
public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;

  /**
   * Procesa la petición generando tokens JWT si existe autenticación válida.
   *
   * <p>Verifica si hay un usuario autenticado en el contexto de seguridad de Spring.
   * Si existe, genera un access token con información del usuario y sus autoridades,
   * y un refresh token para renovación. Ambos tokens se almacenan en cookies seguras
   * HTTP-only y el access token también se incluye en el encabezado Authorization.
   * Registra información de depuración sobre el proceso de generación de tokens.</p>
   *
   * @param request     petición HTTP actual
   * @param response    respuesta HTTP donde se establecerán las cookies y encabezados con los tokens
   * @param filterChain cadena de filtros para continuar el procesamiento de la petición
   * @throws ServletException sí ocurre un error durante el procesamiento del servlet
   * @throws IOException      sí ocurre un error de entrada/salida
   */
  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    // Obtiene la autenticación actual del contexto de seguridad de Spring
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    log.debug("JWTTokenGeneratorFilter ejecutado para: {}", request.getServletPath());
    log.debug("Authentication present: {}", authentication != null);

    // Verifica si hay un usuario autenticado
    if (authentication != null && authentication.isAuthenticated()) {
      log.debug("Usuario autenticado: {}", authentication.getName());
      log.debug("Authorities: {}", authentication.getAuthorities());
      // Generar Access Token
      String accessToken = jwtUtil.generateAccessToken(authentication);
      log.debug("Access token generado (primeros 20 chars): {}",
                accessToken.substring(0, Math.min(20, accessToken.length())));
      // Generar Refresh Token
      String refreshToken = jwtUtil.generateRefreshToken(authentication.getName());
      log.debug("Refresh token generado (primeros 20 chars): {}",
                refreshToken.substring(0, Math.min(20, refreshToken.length())));

      // Almacenar tokens en cookies seguras
      cookieUtil.createAccessTokenCookie(response, accessToken);
      cookieUtil.createRefreshTokenCookie(response, refreshToken);

      // También en header para compatibilidad (opcional)
      response.setHeader("Authorization", "Bearer " + accessToken);
      log.info("Tokens JWT generados y almacenados en cookies para: {}", authentication.getName());
    } else {
      log.warn("No se generaron tokens JWT - Usuario no autenticado en: {}", request.getServletPath());
    }
    // Continúa con la cadena de filtros
    filterChain.doFilter(request, response);
  }


  /**
   * Determina si este filtro debe ejecutarse para la petición actual.
   *
   * <p>Este filtro solo debe procesar peticiones al endpoint de login. Para cualquier
   * otra ruta, el filtro se omite evitando procesamiento innecesario.</p>
   *
   * @param request petición HTTP que se está procesando
   * @return true si el filtro NO debe ejecutarse, false si debe procesarse
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getServletPath().equals("/auth/login");
  }
}
