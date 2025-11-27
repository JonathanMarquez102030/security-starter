package com.jonathanmarquez.security.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jonathanmarquez.security.email_verification.exception.EmailNotVerifiedException;
import com.jonathanmarquez.security.exceptions.helpers.ErrorApiResponseHelper;
import com.jonathanmarquez.security.exceptions.response.ErrorApiResponse;
import com.jonathanmarquez.security.utils.ProfileDetector;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
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
    ErrorInfo errorInfo = determineErrorInfo(authException);


    ErrorApiResponse errorResponse = ErrorApiResponse.builder()
                                                     .success(false)
                                                     .message(errorInfo.message())
                                                     .status(status.getReasonPhrase().toLowerCase())
                                                     .statusCode(status.value())
                                                     .timestamp(LocalDateTime.now())
                                                     .path(path)
                                                     .details(ErrorApiResponseHelper.buildDetails(profileDetector,
                                                                                                  authException, null))
                                                     .build();


    response.setHeader("error-reason", errorInfo.reason());
    response.setStatus(status.value());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }

  private ErrorInfo determineErrorInfo(AuthenticationException authException) {
    if (authException instanceof InternalAuthenticationServiceException) {
      if (authException.getCause() instanceof EmailNotVerifiedException) {
        return new ErrorInfo(authException.getCause().getMessage(), "email_not_verified");
      }
      return new ErrorInfo(authException.getMessage(), "authentication_failed");
    }

    if (authException instanceof DisabledException) {
      return new ErrorInfo("Usuario deshabilitado, contacte con el administrador.", "user_disabled");
    }

    if (authException instanceof BadCredentialsException) {
      return new ErrorInfo("Credenciales inválidas. Usuario o contraseña incorrectos", "invalid_credentials");
    }

    if (authException instanceof InsufficientAuthenticationException) {
      return new ErrorInfo("Autenticación insuficiente. inicie sesión nuevamente", "insufficient_authentication");
    }

    return new ErrorInfo("Error de autenticación.", "authentication_failed");
  }

  private record ErrorInfo(String message, String reason) {
  }
}

