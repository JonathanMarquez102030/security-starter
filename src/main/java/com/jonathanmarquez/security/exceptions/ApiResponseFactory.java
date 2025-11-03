package com.jonathanmarquez.security.exceptions;

import com.jonathanmarquez.security.exceptions.response.ErrorApiResponse;
import com.jonathanmarquez.security.exceptions.response.SuccessApiResponse;
import com.jonathanmarquez.security.utils.ProfileDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Factory para crear respuestas estandarizadas de la API.
 * Gestiona automáticamente los detalles técnicos según el entorno (dev/prod).
 */
@Component
@RequiredArgsConstructor
public class ApiResponseFactory {

  private final ProfileDetector profileDetector;

  // ========== SUCCESS RESPONSES ==========

  /**
   * Crea una respuesta exitosa (200) con datos y path.
   */
  public <T> SuccessApiResponse<T> success(T data, String message, String path) {
    return SuccessApiResponse.<T>builder()
                             .success(true)
                             .message(message)
                             .status(HttpStatus.OK.getReasonPhrase().toLowerCase())
                             .statusCode(HttpStatus.OK.value())
                             .timestamp(LocalDateTime.now())
                             .path(path)
                             .data(data)
                             .build();
  }

  /**
   * Crea una respuesta exitosa (200) con datos sin path.
   */
  public <T> SuccessApiResponse<T> success(T data, String message) {
    return success(data, message, null);
  }

  /**
   * Crea una respuesta de recurso creado (201) con datos y path.
   */
  public <T> SuccessApiResponse<T> created(T data, String message, String path) {
    return SuccessApiResponse.<T>builder()
                             .success(true)
                             .message(message)
                             .status(HttpStatus.CREATED.getReasonPhrase().toLowerCase())
                             .statusCode(HttpStatus.CREATED.value())
                             .timestamp(LocalDateTime.now())
                             .path(path)
                             .data(data)
                             .build();
  }

  /**
   * Crea una respuesta de recurso creado (201) con datos sin path.
   */
  public <T> SuccessApiResponse<T> created(T data, String message) {
    return created(data, message, null);
  }

  /**
   * Crea una respuesta sin contenido (204).
   */
  public SuccessApiResponse<Void> noContent(String message) {
    return SuccessApiResponse.<Void>builder()
                             .success(true)
                             .message(message)
                             .status(HttpStatus.NO_CONTENT.getReasonPhrase().toLowerCase())
                             .statusCode(HttpStatus.NO_CONTENT.value())
                             .timestamp(LocalDateTime.now())
                             .build();
  }

  // ========== ERROR RESPONSES ==========

  /**
   * Crea una respuesta de error genérica con estado HTTP personalizado.
   */
  public ErrorApiResponse error(String message, HttpStatus status, String path, Exception ex) {
    return ErrorApiResponse.builder()
                           .success(false)
                           .message(message)
                           .status(status.getReasonPhrase().toLowerCase())
                           .statusCode(status.value())
                           .timestamp(LocalDateTime.now())
                           .path(path)
                           .details(resolveDetails(ex))
                           .build();
  }

  /**
   * Crea una respuesta de bad request (400) con excepción.
   */
  public ErrorApiResponse badRequest(String message, String path, Exception ex) {
    return ErrorApiResponse.builder()
                           .success(false)
                           .message(message)
                           .status(HttpStatus.BAD_REQUEST.getReasonPhrase().toLowerCase())
                           .statusCode(HttpStatus.BAD_REQUEST.value())
                           .timestamp(LocalDateTime.now())
                           .path(path)
                           .details(resolveDetails(ex))
                           .build();
  }

  /**
   * Crea una respuesta de bad request (400) sin excepción.
   */
  public ErrorApiResponse badRequest(String message, String path) {
    return badRequest(message, path, null);
  }

  /**
   * Crea una respuesta de no autorizado (401).
   */
  public ErrorApiResponse unauthorized(String message, String path) {
    return ErrorApiResponse.builder()
                           .success(false)
                           .message(message)
                           .status(HttpStatus.UNAUTHORIZED.getReasonPhrase().toLowerCase())
                           .statusCode(HttpStatus.UNAUTHORIZED.value())
                           .timestamp(LocalDateTime.now())
                           .path(path)
                           .build();
  }

  /**
   * Crea una respuesta de prohibido (403).
   */
  public ErrorApiResponse forbidden(String message, String path) {
    return ErrorApiResponse.builder()
                           .success(false)
                           .message(message)
                           .status(HttpStatus.FORBIDDEN.getReasonPhrase().toLowerCase())
                           .statusCode(HttpStatus.FORBIDDEN.value())
                           .timestamp(LocalDateTime.now())
                           .path(path)
                           .build();
  }

  /**
   * Crea una respuesta de recurso no encontrado (404).
   */
  public ErrorApiResponse notFound(String message, String path) {
    return ErrorApiResponse.builder()
                           .success(false)
                           .message(message)
                           .status(HttpStatus.NOT_FOUND.getReasonPhrase().toLowerCase())
                           .statusCode(HttpStatus.NOT_FOUND.value())
                           .timestamp(LocalDateTime.now())
                           .path(path)
                           .build();
  }

  /**
   * Crea una respuesta de conflicto (409).
   */
  public ErrorApiResponse conflict(String message, String path) {
    return ErrorApiResponse.builder()
                           .success(false)
                           .message(message)
                           .status(HttpStatus.CONFLICT.getReasonPhrase().toLowerCase())
                           .statusCode(HttpStatus.CONFLICT.value())
                           .timestamp(LocalDateTime.now())
                           .path(path)
                           .build();
  }

  /**
   * Crea una respuesta de error interno del servidor (500).
   */
  public ErrorApiResponse internalError(String message, String path, Exception ex) {
    return ErrorApiResponse.builder()
                           .success(false)
                           .message(message)
                           .status(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase().toLowerCase())
                           .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                           .timestamp(LocalDateTime.now())
                           .path(path)
                           .details(resolveDetails(ex))
                           .build();
  }

  // ========== MÉTODOS PRIVADOS ==========

  /**
   * Resuelve los detalles técnicos según el entorno.
   * Solo muestra información técnica en desarrollo.
   *
   * @param ex excepción de la cual extraer el mensaje
   * @return mensaje técnico si está en desarrollo, null si está en producción
   */
  private String resolveDetails(Exception ex) {
    if (ex == null || profileDetector.isProfileActive("prod")) {
      return null;
    }
    return ex.getMessage();
  }
}