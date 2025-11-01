package com.jonathanmarquez.security.security.filter;

import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
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

    // Verifica si hay un usuario autenticado
    if (authentication != null && authentication.isAuthenticated()) {

      // Generar Access Token
      String accessToken = jwtUtil.generateAccessToken(authentication);

      // Generar Refresh Token
      String refreshToken = jwtUtil.generateRefreshToken(authentication.getName());

      // Almacenar tokens en cookies seguras
      cookieUtil.createAccessTokenCookie(response, accessToken);
      cookieUtil.createRefreshTokenCookie(response, refreshToken);

      // También en header para compatibilidad (opcional)
      response.setHeader("Authorization", "Bearer " + accessToken);
    }
    // Continúa con la cadena de filtros
    filterChain.doFilter(request, response);
  }


  /**
   * Determina si el request que se esta procesando debe ser filtrado por este
   * Filtro. En este caso, solo se filtran las peticiones que tengan como
   * servletPath a "/user".
   *
   * @param request La peticion que se esta procesando
   * @return boolean true si el request no debe ser filtrado, false en caso
   * contrario
   * @throws ServletException si ocurre un error al procesar el request
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return !request.getServletPath().equals("/api/auth/login");
  }
}
