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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JWTTokenValidatorFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    log.debug("JWTTokenValidatorFilter ejecutado para: {}", request.getServletPath());


    String jwt = null;

    // 1. Intentar obtener token desde cookie (prioridad)
    jwt = cookieUtil.getAccessTokenFromCookie(request);
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
          String authorities = jwtUtil.extractAuthorities(jwt);

          log.debug("JWT válido para usuario: {} con authorities: {}", username, authorities);

          List<GrantedAuthority> grantedAuthorities =
              AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

          Authentication authentication =
              new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);

          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          log.warn("JWT inválido o expirado");
          throw new BadCredentialsException("Invalid or expired JWT Token");
        }
      } catch (Exception e) {
        log.error("Error validando JWT: {}", e.getMessage());
        throw new BadCredentialsException("Invalid JWT Token: " + e.getMessage());
      }
    }else{
      log.debug("No se encontró JWT en cookies ni headers para: {}", request.getServletPath());
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Determina si el request que se esta procesando debe ser filtrado por este
   * Filtro. En este caso, solo se filtran las peticiones que NO tengan como
   * servletPath a "/user".
   *
   * @param request La petición que se esta procesando
   * @return boolean true si el request no debe ser filtrado, false en caso
   * contrario
   * @throws ServletException si ocurre un error al procesar el request
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//    boolean shouldNotFilter = request.getServletPath().equals("/auth/login");
//    if( request.getServletPath().equals("/auth/register")) {
//      shouldNotFilter = true;
//    }
//    return shouldNotFilter;

    String path = request.getServletPath();
    return path.equals("/api/auth/login") ||
        path.equals("/api/auth/register") ||
        path.equals("/api/auth/refresh") ||
        path.equals("/api/auth/csrf");
  }
}
