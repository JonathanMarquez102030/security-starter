package com.jonathanmarquez.security.security.utils;

import com.jonathanmarquez.security.security.enums.SpringProfile;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

@Slf4j
@RequiredArgsConstructor
public class CookieUtil {

  private static final String ACCESS_TOKEN_COOKIE = "accessToken";
  private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
  private static final String PWD_RESET_TOKEN_COOKIE = "pwdResetToken";

  private final Environment env;

  /**
   * Crea una cookie segura para el Access Token.
   */
  public void createAccessTokenCookie(HttpServletResponse response, String token) {
    int maxAge = Integer.parseInt(env.getProperty("jwt.access.expiration", "900000")) / 1000; // convertir a segundos
    log.debug("Creando accessToken cookie con maxAge: {} segundos", maxAge);
    createSecureCookie(response, ACCESS_TOKEN_COOKIE, token, maxAge);
  }

  /**
   * Crea una cookie segura para el Refresh Token.
   */
  public void createRefreshTokenCookie(HttpServletResponse response, String token) {
    int maxAge = Integer.parseInt(
        env.getProperty("jwt.refresh.expiration", "604800000")) / 1000; // convertir a segundos
    log.debug("Creando refreshToken cookie con maxAge: {} segundos", maxAge);
    createSecureCookie(response, REFRESH_TOKEN_COOKIE, token, maxAge);
  }

  /**
   * Crea una cookie segura con las mejores prácticas.
   */
  private void createSecureCookie(HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true); // No accesible desde JavaScript
    cookie.setSecure(isProductionMode()); // Solo HTTPS en producción
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    cookie.setAttribute("SameSite", "Strict"); // Protección CSRF adicional
    response.addCookie(cookie);

    log.debug("Cookie '{}' creada - HttpOnly: true, Secure: {}, MaxAge: {}, SameSite: Strict",
              name, isProductionMode(), maxAge);
  }

  /**
   * Obtiene el Access Token desde las cookies.
   */
  public String getAccessTokenFromCookie(HttpServletRequest request) {
    return getCookieValue(request, ACCESS_TOKEN_COOKIE);
  }

  /**
   * Obtiene el Refresh Token desde las cookies.
   */
  public String getRefreshTokenFromCookie(HttpServletRequest request) {
    return getCookieValue(request, REFRESH_TOKEN_COOKIE);
  }

  /**
   * Obtiene el valor de una cookie específica.
   */
  private String getCookieValue(HttpServletRequest request, String name) {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (name.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  /**
   * Obtiene el Password Reset Token desde las cookies.
   */
  public String getPasswordResetTokenFromCookie(HttpServletRequest request) {
    return getCookieValue(request, PWD_RESET_TOKEN_COOKIE);
  }

  /**
   * Crea una cookie segura para el Password Reset Token (TTL corto).
   */
  public void createPasswordResetTokenCookie(HttpServletResponse response, String token) {
    int maxAge = Integer.parseInt(
        env.getProperty("jwt.pwd_reset.expiration", "600000")) / 1000; // ms -> seg
    log.debug("Creando pwdResetToken cookie con maxAge: {} segundos", maxAge);
    createSecureCookie(response, PWD_RESET_TOKEN_COOKIE, token, maxAge);
  }

  /**
   * Elimina la cookie del Password Reset Token.
   */
  public void deletePasswordResetTokenCookie(HttpServletResponse response) {
    deleteCookie(response, PWD_RESET_TOKEN_COOKIE);
  }

  /**
   * Elimina las cookies de tokens (para logout).
   */
  public void deleteTokenCookies(HttpServletResponse response) {
    deleteCookie(response, ACCESS_TOKEN_COOKIE);
    deleteCookie(response, REFRESH_TOKEN_COOKIE);
    deleteCookie(response, PWD_RESET_TOKEN_COOKIE);
  }

  /**
   * Elimina una cookie específica.
   */
  private void deleteCookie(HttpServletResponse response, String name) {
    Cookie cookie = new Cookie(name, null);
    cookie.setHttpOnly(true);
    cookie.setSecure(isProductionMode());
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  /**
   * Verifica si estamos en modo producción.
   */
  private boolean isProductionMode() {
    String[] activeProfiles = env.getActiveProfiles();
    for (String profile : activeProfiles) {
      if (SpringProfile.PROD.getProfileName().equals(profile)) {
        return true;
      }
    }
    return false;
  }
}