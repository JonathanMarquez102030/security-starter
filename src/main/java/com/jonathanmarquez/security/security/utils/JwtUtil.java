package com.jonathanmarquez.security.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class JwtUtil {


  private Environment env;

  private SecretKey getSigningKey() {
    String secret = env.getProperty("jwt.secret.key", "default_secret_min_length_256bits");
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Genera un Access Token JWT (corta duración).
   */
  public String generateAccessToken(Authentication authentication) {
    long expirationTime = Long.parseLong(
        env.getProperty("jwt.access.expiration", "900000") // 15 minutos por defecto
    );

    return Jwts.builder()
               .issuer(env.getProperty("jwt.issuer", "E-Commerce-App"))
               .subject(authentication.getName())
               .claim("username", authentication.getName())
               .claim("authorities", authentication.getAuthorities().stream()
                                                   .map(GrantedAuthority::getAuthority)
                                                   .collect(Collectors.joining(",")))
               .claim("type", "ACCESS")
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
               .issuer(env.getProperty("jwt.issuer", "E-Commerce-App"))
               .subject(username)
               .claim("username", username)
               .claim("type", "REFRESH")
               .issuedAt(new Date())
               .expiration(new Date(System.currentTimeMillis() + expirationTime))
               .signWith(getSigningKey())
               .compact();
  }

  /**
   * Extrae el username del token JWT.
   */
  public String extractUsername(String token) {
    return extractClaim(token, claims -> String.valueOf(claims.get("username")));
  }

  /**
   * Extrae las autoridades del token JWT.
   */
  public String extractAuthorities(String token) {
    return extractClaim(token, claims -> String.valueOf(claims.get("authorities")));
  }

  /**
   * Extrae el tipo de token (ACCESS o REFRESH).
   */
  public String extractTokenType(String token) {
    return extractClaim(token, claims -> String.valueOf(claims.get("type")));
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
   * Extrae todos los claims del token JWT.
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
               .verifyWith(getSigningKey())
               .build()
               .parseSignedClaims(token)
               .getPayload();
  }

  /**
   * Verifica si el token ha expirado.
   */
  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Valida si el token es válido (no expirado y tipo correcto).
   */
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
}