/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.filter;

import com.jonathanmarquezperez.security.model.SecurityUserDetails;
import com.jonathanmarquezperez.utils.CookieUtil;
import com.jonathanmarquezperez.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que valida tokens JWT y establece la autenticación en el contexto de seguridad.
 *
 * <p>Este filtro intercepta todas las peticiones HTTP, extrae el token JWT desde cookies
 * o el encabezado Authorization, valida su integridad y vigencia, y si es válido, carga
 * los detalles del usuario y establece la autenticación en el contexto de Spring Security.
 * Esto permite que peticiones subsecuentes accedan a información del usuario autenticado
 * sin necesidad de credenciales en cada petición.</p>
 */
@RequiredArgsConstructor
@Slf4j
public class JWTTokenValidatorFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final UserDetailsService userDetailsService;

  /**
   * Procesa cada petición HTTP validando el token JWT y estableciendo la autenticación.
   *
   * <p>Extrae el access token JWT primero desde cookies (prioridad) y luego desde el
   * encabezado Authorization si no se encuentra en cookies. Si el token es válido,
   * extrae el nombre de usuario, carga los detalles completos del usuario incluyendo
   * su perfil y autoridades, crea un objeto de autenticación y lo establece en el
   * contexto de seguridad de Spring. Si el token es inválido o no existe, la petición
   * continúa sin autenticación establecida.</p>
   *
   * @param request     petición HTTP actual de la cual extraer el token JWT
   * @param response    respuesta HTTP actual
   * @param filterChain cadena de filtros para continuar el procesamiento de la petición
   * @throws ServletException si ocurre un error durante el procesamiento del servlet
   * @throws IOException      si ocurre un error de entrada/salida
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getServletPath();
    log.debug("JWTTokenValidatorFilter ejecutándose para: {}", path);


    // Intenta obtener token desde cookie (prioridad)
    String jwt = cookieUtil.getAccessTokenFromCookie(request);
    if (jwt != null) {
      log.debug("Access token encontrado en cookie");
    }

    // Si no hay en cookie, intentar desde header Authorization
    if (jwt == null) {
      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        jwt = authHeader.substring(7);
        log.debug("Access token encontrado en header Authorization");
      }
    }

    // Validar y autenticar
    if (jwt != null) {
      try {
        if (jwtUtil.isAccessTokenValid(jwt)) {
          String username = jwtUtil.extractUsername(jwt);
          log.debug("JWT válido para usuario: {}", username);

          // Cargar usuario completo con su perfil
          SecurityUserDetails userDetails = (SecurityUserDetails) userDetailsService.loadUserByUsername(username);

          Authentication authentication = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );

          SecurityContextHolder.getContext().setAuthentication(authentication);
          log.debug("Usuario autenticado vía JWT: {}", username);
        } else {
          log.warn("JWT inválido o expirado");
        }
      } catch (Exception e) {
        log.error("Error validando JWT: {}", e.getMessage());
      }
    } else {
      log.debug("No se encontró JWT en cookies ni headers para: {}", request.getServletPath());
    }

    filterChain.doFilter(request, response);
  }
}
