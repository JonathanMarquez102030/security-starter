package com.jonathanmarquez.security.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que asegura que el token CSRF se envíe en cada respuesta,
 * incluso después de que el CsrfFilter lo reemplace.
 */
@Slf4j
public class CsrfCookieFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {

    // Wrapper que intercepta cuando la respuesta está por enviarse
    HttpServletResponse wrappedResponse = new HttpServletResponseWrapper(response) {
      private boolean cookieAdded = false;

      @Override
      public void flushBuffer() throws IOException {
        addCsrfCookieIfNeeded();
        super.flushBuffer();
      }

      @Override
      public ServletOutputStream getOutputStream() throws IOException {
        addCsrfCookieIfNeeded();
        return super.getOutputStream();
      }

      @Override
      public java.io.PrintWriter getWriter() throws IOException {
        addCsrfCookieIfNeeded();
        return super.getWriter();
      }

      private void addCsrfCookieIfNeeded() {
        if (!cookieAdded && !isCommitted()) {
          CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
          if (token != null) {
            String tokenValue = token.getToken();
            log.debug("Enviando token CSRF en respuesta: {} {}", request.getMethod(), request.getRequestURI());

            Cookie cookie = new Cookie("XSRF-TOKEN", tokenValue);
            cookie.setPath("/");
            cookie.setHttpOnly(false);
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
            addCookie(cookie);
            cookieAdded = true;
          }
        }
      }
    };

    // Fuerza la carga inicial del token
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    if (csrfToken != null) {
      csrfToken.getToken();
    }

    filterChain.doFilter(request, wrappedResponse);
  }
}