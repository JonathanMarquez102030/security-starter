package com.jonathanmarquez.security.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtUtil {


  private final Environment env;

  @Value("${spring.application.name}")
  private String appName;

  //JWT CLAIMS
  private static final String USERNAME = "username";
  private static final String AUTHORITIES = "authorities";
  private static final String TYPE = "type";
  private static final String JTI = "jti";

  /**
   * Genera un Access Token JWT (corta duración).
   */
  public String generateAccessToken(Authentication authentication) {
    long expirationTime = Long.parseLong(
        env.getProperty("jwt.access.expiration", "900000") // 15 minutos por defecto
    );

    return Jwts.builder()
               .issuer(env.getProperty("jwt.issuer", appName))
               .subject(authentication.getName())
               .claim(USERNAME, authentication.getName())
               .claim(AUTHORITIES, authentication.getAuthorities().stream()
                                                 .map(GrantedAuthority::getAuthority)
                                                 .collect(Collectors.joining(",")))
               .claim(TYPE, "ACCESS")
               .issuedAt(new Date())
               .expiration(new Date(System.currentTimeMillis() + expirationTime))
               .signWith(getSigningKey())
               .compact();
  }

  /**
   * Genera un Refresh Token JWT (larga duración).
   */
  public String generateRefreshToken(String username) {
    long expirationTime = Long.parseLong(
        env.getProperty("jwt.refresh.expiration", "604800000") // 7 días por defecto
    );

    return Jwts.builder()
               .issuer(env.getProperty("jwt.issuer", appName))
               .subject(username)
               .claim(USERNAME, username)
               .claim(TYPE, "REFRESH")
               .issuedAt(new Date())
               .expiration(new Date(System.currentTimeMillis() + expirationTime))
               .signWith(getSigningKey())
               .compact();
  }

  /**
   * Genera un Password Reset Token JWT (TTL corto).
   * Claims:
   * - subject=username/email
   * - username=email
   * - type=PWD_RESET
   * - jti=UUID (para invalidación opcional vía blacklist)
   */
  public String generatePasswordResetToken(String email) {
    long expirationTime = Long.parseLong(
        env.getProperty("jwt.pwd_reset.expiration", "600000") // 10 minutos por defecto
    );

    return Jwts.builder()
               .issuer(env.getProperty("jwt.issuer", appName))
               .subject(email)
               .claim(USERNAME, email)
               .claim(TYPE, "PWD_RESET")
               .claim(JTI, UUID.randomUUID().toString())
               .issuedAt(new Date())
               .expiration(new Date(System.currentTimeMillis() + expirationTime))
               .signWith(getSigningKey())
               .compact();
  }

  /**
   * Extrae el username del token JWT.
   */
  public String extractUsername(String token) {
    return extractClaim(token, claims -> String.valueOf(claims.get(USERNAME)));
  }

  /**
   * Extrae las autoridades del token JWT.
   */
  public String extractAuthorities(String token) {
    return extractClaim(token, claims -> String.valueOf(claims.get(AUTHORITIES)));
  }

  /**
   * Extrae el tipo de token (ACCESS o REFRESH).
   */
  public String extractTokenType(String token) {
    return extractClaim(token, claims -> String.valueOf(claims.get(TYPE)));
  }

  /**
   * Extrae jti del token (si existe).
   */
  @SuppressWarnings("unused")
  public String extractJti(String token) {
    return extractClaim(token, claims -> String.valueOf(claims.get(JTI)));
  }

  /**
   * Extrae la fecha de expiración del token.
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extrae un claim específico del token JWT.
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }


  /**
   * Valida si el token es válido (no expirado y tipo correcto).
   */
  @SuppressWarnings("unused")
  public Boolean isTokenValid(String token) {
    try {
      return !isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Valida si es un access token válido.
   */
  public Boolean isAccessTokenValid(String token) {
    try {
      return !isTokenExpired(token) && "ACCESS".equals(extractTokenType(token));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Valida si es un refresh token válido.
   */
  public Boolean isRefreshTokenValid(String token) {
    try {
      return !isTokenExpired(token) && "REFRESH".equals(extractTokenType(token));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Valida si es un password reset token válido.
   */
  public Boolean isPasswordResetTokenValid(String token) {
    try {
      return !isTokenExpired(token) && "PWD_RESET".equals(extractTokenType(token));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Verifica si el token ha expirado.
   */
  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private SecretKey getSigningKey() {
    String secret = env.getProperty("jwt.secret.key", "default_secret_min_length_256bits");
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Extrae todos los claims del token JWT.
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
               .verifyWith(getSigningKey())
               .build()
               .parseSignedClaims(token)
               .getPayload();
  }
}