/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.utils;

import com.jonathanmarquezperez.security.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JwtUtil {

  //JWT CLAIMS
  private static final String USERNAME = "username";
  private static final String AUTHORITIES = "authorities";
  private static final String TYPE = "type";
  private static final String JTI = "jti";

  private final String JWT_ISSUER;

  private final SecurityProperties securityProperties;

  public JwtUtil(SecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
    JWT_ISSUER = securityProperties.getJwt().getIssuer();
  }

  /**
   * Genera un Access Token JWT (corta duración).
   */
  public String generateAccessToken(Authentication authentication) {
    long expirationTime = securityProperties.getJwt().getAccessExpirationTime();

    return Jwts.builder()
               .issuer(JWT_ISSUER)
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
    long expirationTime = securityProperties.getJwt().getRefreshExpirationTime();

    return Jwts.builder()
               .issuer(JWT_ISSUER)
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
    long expirationTime = securityProperties.getJwt().getPasswordResetExpirationTime();

    return Jwts.builder()
               .issuer(JWT_ISSUER)
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
    String secret = securityProperties.getJwt().getSecretKey();
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
