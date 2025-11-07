package com.jonathanmarquez.security.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jonathanmarquez.security.exceptions.helpers.ErrorApiResponseHelper;
import com.jonathanmarquez.security.exceptions.response.ErrorApiResponse;
import com.jonathanmarquez.security.utils.ProfileDetector;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ProfileDetector profileDetector;
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException {

    HttpStatus status = HttpStatus.UNAUTHORIZED;
    String path = request.getRequestURI();
//    String message = "Credenciales inválidas, token caducado o token no válido";
    String message = determineErrorMessage(authException);
    String errorReason = determineErrorReason(authException);

                                                  ErrorApiResponse errorResponse = ErrorApiResponse.builder()
                                                     .success(false)
                                                     .message(message)
                                                     .status(status.getReasonPhrase().toLowerCase())
                                                     .statusCode(status.value())
                                                     .timestamp(LocalDateTime.now())
                                                     .path(path)
                                                     .details(ErrorApiResponseHelper.buildDetails(profileDetector, authException, null))
                                                     .build();


    response.setHeader("error-reason", errorReason);
    response.setStatus(status.value());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }

  private String determineErrorMessage(AuthenticationException authException) {
    // Verificar si es por credenciales incorrectas
    if (authException instanceof BadCredentialsException) {
      return "Credenciales inválidas. Usuario o contraseña incorrectos";
    }

    // Verificar causas relacionadas con JWT
    Throwable cause = authException.getCause();

    if (cause instanceof ExpiredJwtException) {
      return "Token expirado. Por favor, inicie sesión nuevamente";
    }

    if (cause instanceof SignatureException) {
      return "Token inválido. Firma no válida";
    }

    if (cause instanceof MalformedJwtException) {
      return "Token malformado. Formato no válido";
    }

    // Verificar por tipo de excepción de autenticación insuficiente
    if (authException instanceof InsufficientAuthenticationException) {
      String exceptionMessage = authException.getMessage();

      if (exceptionMessage != null) {
        if (exceptionMessage.contains("expired")) {
          return "Token expirado. Por favor, inicie sesión nuevamente";
        }
        if (exceptionMessage.contains("invalid") || exceptionMessage.contains("malformed")) {
          return "Token inválido o malformado";
        }
      }

      return "Autenticación insuficiente. inicie sesión nuevamente";
    }

    // Mensaje genérico si no se puede determinar el tipo específico
    return "Error de autenticación. Credenciales inválidas, token caducado o token no válido";
  }

  private String determineErrorReason(AuthenticationException authException) {
    if (authException instanceof BadCredentialsException) {
      return "invalid_credentials";
    }

    Throwable cause = authException.getCause();

    if (cause instanceof ExpiredJwtException) {
      return "token_expired";
    }

    if (cause instanceof SignatureException) {
      return "invalid_token_signature";
    }

    if (cause instanceof MalformedJwtException) {
      return "malformed_token";
    }

    if (authException instanceof InsufficientAuthenticationException) {
      String exceptionMessage = authException.getMessage();
      if (exceptionMessage != null && exceptionMessage.contains("expired")) {
        return "token_expired";
      }
      return "insufficient_authentication";
    }

    return "authentication_failed";
  }
}
