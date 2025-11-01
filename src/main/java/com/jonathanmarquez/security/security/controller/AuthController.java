package com.jonathanmarquez.security.security.controller;

import com.jonathanmarquez.security.security.model.SecurityUserDetails;
import com.jonathanmarquez.security.security.model.dto.AuthResponseDto;
import com.jonathanmarquez.security.security.model.dto.RegisterRequestDto;
import com.jonathanmarquez.security.security.service.CustomUserDetailsService;
import com.jonathanmarquez.security.security.service.UserProfileService;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador de autenticación con JWT + Cookies seguras.
 *
 * Flujo de autenticación:
 * 1. Cliente envía credenciales vía Basic Auth a /api/auth/login
 * 2. BasicAuthenticationFilter valida credenciales
 * 3. JWTTokenGeneratorFilter genera tokens JWT
 * 4. Tokens se almacenan en cookies seguras HttpOnly
 * 5. Requests subsiguientes usan JWT desde cookies (validados por JWTTokenValidatorFilter)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserProfileService userProfileService;
  private final CustomUserDetailsService userDetailsService;
  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;

  /**
   * Login: Spring Security maneja autenticación via BasicAuthenticationFilter.
   * JWTTokenGeneratorFilter genera los tokens automáticamente.
   *
   * GET /api/auth/login
   * Headers: Authorization: Basic base64(username:password)
   *
   * Respuesta:
   * - Cookies: accessToken, refreshToken (HttpOnly, Secure en prod, SameSite=Strict)
   * - Body: Información del usuario autenticado
   */
  @GetMapping("/login")
  public ResponseEntity<?> login(Authentication authentication) {

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                           .body(Map.of("success", false, "message", "No autenticado"));
    }

    SecurityUserDetails user = (SecurityUserDetails) authentication.getPrincipal();
    log.info("Login exitoso: {}", user.getUsername());

    AuthResponseDto response = buildAuthResponse(user);

    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "Login exitoso",
        "data", response
    ));
  }

  /**
   * Registro de nuevos usuarios.
   * POST /api/auth/register
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto request) {
    try {

      if (userProfileService.userExists(request.username())) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "El username ya está registrado"
        ));
      }

      var profile = userProfileService.createUser(
          request.username(),
          request.password(),
          request.email(),
          List.of("ROLE_USER")
      );

      profile.setFirstName(request.firstName());
      profile.setLastName(request.lastName());
      profile.setPhone(request.phone());
      userProfileService.updateProfile(profile);

      log.info("Usuario registrado: {}", request.username());

      return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
          "success", true,
          "message", "Usuario registrado exitosamente",
          "data", Map.of(
              "username", profile.getUsername(),
              "email", profile.getEmail()
          )
      ));
    } catch (Exception e) {
      log.error("Error en registro: {}", e.getMessage());
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "Error al registrar: " + e.getMessage()
      ));
    }
  }

  /**
   * Obtener usuario autenticado actual.
   * GET /api/auth/me
   */
  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(Authentication  authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                           .body(Map.of("success", false, "message", "No autenticado"));
    }

    SecurityUserDetails user;

    if (authentication.getPrincipal() instanceof SecurityUserDetails) {
      // Autenticación vía Basic Auth (login directo)
      user = (SecurityUserDetails) authentication.getPrincipal();
    } else {
      // Autenticación vía JWT - cargar usuario completo
      String username = authentication.getName();
      user = (SecurityUserDetails) userDetailsService.loadUserByUsername(username);
    }

    AuthResponseDto response = buildAuthResponse(user);

    return ResponseEntity.ok(Map.of(
        "success", true,
        "data", response
    ));
  }

  /**
   * Refresh token: renueva el access token usando el refresh token.
   * POST /api/auth/refresh
   */
  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    try {
      // Obtener refresh token desde cookie
      String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);

      if (refreshToken == null || !jwtUtil.isRefreshTokenValid(refreshToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(Map.of("success", false, "message", "Refresh token inválido o expirado"));
      }

      // Extraer username y generar nuevo access token
      String username = jwtUtil.extractUsername(refreshToken);
      String authorities = jwtUtil.extractAuthorities(refreshToken);

      // Crear autenticación temporal
      Authentication auth = new UsernamePasswordAuthenticationToken(
          username,
          null,
          AuthorityUtils.commaSeparatedStringToAuthorityList(
              authorities != null && !authorities.equals("null") ? authorities : ""
          )
      );

      // Generar nuevo access token
      String newAccessToken = jwtUtil.generateAccessToken(auth);
      cookieUtil.createAccessTokenCookie(response, newAccessToken);

      log.info("Token renovado para: {}", username);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", "Token renovado exitosamente"
      ));

    } catch (Exception e) {
      log.error("Error al renovar token: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                           .body(Map.of("success", false, "message", "Error al renovar token"));
    }
  }

  /**
   * Logout: elimina cookies y limpia contexto.
   * POST /api/auth/logout
   */
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
    cookieUtil.deleteTokenCookies(response);
    SecurityContextHolder.clearContext();

    log.info("Logout exitoso");

    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "Logout exitoso"
    ));
  }

  /**
   * Endpoint para obtener CSRF token.
   * GET /api/auth/csrf
   */
  @GetMapping("/csrf")
  public ResponseEntity<Void> getCsrfToken() {
    return ResponseEntity.ok().build();
  }

  // ===========================
  // Métodos Auxiliares
  // ===========================

  private AuthResponseDto buildAuthResponse(SecurityUserDetails user) {
    return AuthResponseDto.builder()
                          .username(user.getUsername())
                          .email(user.getEmail())
                          .firstName(user.getProfile() != null ? user.getProfile().getFirstName() : null)
                          .lastName(user.getProfile() != null ? user.getProfile().getLastName() : null)
                          .fullName(user.getFullName())
                          .phone(user.getProfile() != null ? user.getProfile().getPhone() : null)
                          .authorities(user.getAuthorities().stream()
                                           .map(auth -> auth.getAuthority())
                                           .toList())
                          .enabled(user.isEnabled())
                          .build();
  }
}