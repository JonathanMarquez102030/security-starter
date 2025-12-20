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

/**
 * Punto de entrada personalizado para manejar excepciones de autenticación en Spring Security.
 *
 * <p>Esta clase intercepta las excepciones de autenticación que ocurren cuando un usuario
 * no autenticado o con credenciales inválidas intenta acceder a recursos protegidos.
 * Genera respuestas JSON estructuradas con código HTTP 401 (Unauthorized) y mensajes
 * específicos según el tipo de error de autenticación detectado.</p>
 */
@RequiredArgsConstructor
public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ProfileDetector profileDetector;
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  /**
   * Maneja las excepciones de autenticación generando una respuesta JSON con el error específico.
   *
   * <p>Determina el tipo de error de autenticación ocurrido, construye una respuesta estructurada
   * con el código de estado 401, un mensaje descriptivo apropiado para cada tipo de excepción,
   * y establece encabezados HTTP con la razón del error. La respuesta incluye detalles adicionales
   * según el perfil activo y se escribe en formato JSON con codificación UTF-8.</p>
   *
   * @param request       solicitud HTTP que generó la excepción de autenticación
   * @param response      respuesta HTTP donde se escribirá el error en formato JSON
   * @param authException excepción de autenticación lanzada durante el proceso de autenticación
   * @throws IOException si ocurre un error al escribir la respuesta
   */
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

  /**
   * Determina el mensaje y razón del error según el tipo de excepción de autenticación.
   *
   * <p>Analiza la excepción recibida y retorna información específica para cada caso:
   * correo no verificado, usuario deshabilitado, credenciales inválidas, autenticación
   * insuficiente, o un error genérico de autenticación. Maneja casos especiales como
   * excepciones encadenadas para proporcionar mensajes más precisos.</p>
   *
   * @param authException excepción de autenticación a analizar
   * @return objeto ErrorInfo conteniendo el mensaje descriptivo y la razón técnica del error
   */
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

  /**
   * Registro que encapsula la información de un error de autenticación.
   *
   * @param message mensaje descriptivo del error para mostrar al usuario
   * @param reason  razón técnica del error para uso en encabezados HTTP y logs
   */
  private record ErrorInfo(String message, String reason) {
  }
}

