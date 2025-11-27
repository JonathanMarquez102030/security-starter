package com.jonathanmarquez.security.security.filter;

import com.jonathanmarquez.security.security.model.SecurityUserDetails;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JWTTokenValidatorFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getServletPath();
    log.debug("JWTTokenValidatorFilter ejecutándose para: {}", path);


    // 1. Intentar obtener token desde cookie (prioridad)
    String jwt = cookieUtil.getAccessTokenFromCookie(request);
    if (jwt != null) {
      log.debug("Access token encontrado en cookie");
    }

    // 2. Si no hay en cookie, intentar desde header Authorization
    if (jwt == null) {
      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        jwt = authHeader.substring(7);
        log.debug("Access token encontrado en header Authorization");
      }
    }

    // 3. Validar y autenticar
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
