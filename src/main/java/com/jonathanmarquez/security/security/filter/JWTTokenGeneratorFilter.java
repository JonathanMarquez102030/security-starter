package com.jonathanmarquez.security.security.filter;

import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;

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
    }
    else{
      log.warn("No se generaron tokens JWT - Usuario no autenticado en: {}", request.getServletPath());
    }
    // Continúa con la cadena de filtros
    filterChain.doFilter(request, response);
  }


  /**
   * Determina si el request que se está procesando debe ser filtrado por este
   * Filtro. En este caso, solo se filtran las peticiones que tengan como
   * servletPath a "/user".
   *
   * @param request La peticion que se está procesando
   * @return boolean true si el request no debe ser filtrado, false en caso
   * contrario
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getServletPath().equals("/api/auth/login");
  }
}
