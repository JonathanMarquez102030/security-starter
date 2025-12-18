package com.jonathanmarquez.security.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que fuerza la generación y envío del token CSRF en cookies.
 *
 * <p>Este filtro se ejecuta una vez por cada petición HTTP y asegura que el token CSRF
 * sea generado y enviado al cliente mediante una cookie. Al invocar el proceso {@code getToken()},
 * se activa el mecanismo de Spring Security para crear y enviar la cookie con el token CSRF,
 * permitiendo que las aplicaciones cliente puedan acceder al token para incluirlo en
 * peticiones posteriores que requieran protección CSRF.</p>
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

  /**
   * Procesa cada petición HTTP forzando la generación del token CSRF.
   *
   * <p>Extrae el token CSRF del atributo de la petición y llama al proceso {@code getToken()}
   * para forzar su generación y envío en la cookie de respuesta. Esto es necesario porque
   * Spring Security solo genera y envía el token CSRF de forma lazy cuando se accede a él
   * explícitamente. Después de forzar la generación, continúa la cadena de filtros normalmente.</p>
   *
   * @param request petición HTTP actual
   * @param response respuesta HTTP donde se establecerá la cookie del token CSRF
   * @param filterChain cadena de filtros para continuar el procesamiento de la petición
   * @throws ServletException sí ocurre un error durante el procesamiento del servlet
   * @throws IOException sí ocurre un error de entrada/salida
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

    // Fuerza la generación del token CSRF para que se envíe en la cookie
    if (csrfToken != null) {
      csrfToken.getToken();
    }

    filterChain.doFilter(request, response);

  }
}
