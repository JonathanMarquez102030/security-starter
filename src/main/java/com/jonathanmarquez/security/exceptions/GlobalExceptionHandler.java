package com.jonathanmarquez.security.exceptions;

import com.jonathanmarquez.security.exceptions.customexceptions.ConflictException;
import com.jonathanmarquez.security.exceptions.customexceptions.ForbiddenException;
import com.jonathanmarquez.security.exceptions.customexceptions.ResourceNotFoundException;
import com.jonathanmarquez.security.exceptions.customexceptions.UnauthorizedException;
import com.jonathanmarquez.security.exceptions.response.ErrorApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la aplicación.
 *
 * <p>Esta clase captura y maneja excepciones lanzadas en los controladores, devolviendo respuestas
 * HTTP estandarizadas con detalles del error.S
 *
 * @version 1.0
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler  {

  private final ApiResponseFactory responseFactory;

  /**
   * Extrae el path limpio del WebRequest.
   */
  private String getPath(WebRequest request) {
    return request.getDescription(false).replace("uri=", "");
  }

  /**
   * Maneja errores de validación de Spring (@Valid).
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorApiResponse> handleValidation(
      MethodArgumentNotValidException ex, WebRequest request) {

    String errorDetails = ex.getBindingResult()
                            .getFieldErrors()
                            .stream()
                            .map(error -> error.getField() + ": " + error.getDefaultMessage())
                            .collect(Collectors.joining(", "));

    ErrorApiResponse response = responseFactory.badRequest(
        "Error de validación en los datos enviados", getPath(request), ex);

    // Sobrescribir details con información de validación específica
    response.setDetails(errorDetails);

    return ResponseEntity.badRequest().body(response);
  }

  /**
   * Maneja excepciones de recurso no encontrado.
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorApiResponse> handleNotFound(
      ResourceNotFoundException ex, WebRequest request) {

    ErrorApiResponse response = responseFactory.notFound(
        ex.getMessage(), getPath(request));

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /**
   * Maneja excepciones de autenticación.
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorApiResponse> handleUnauthorized(
      UnauthorizedException ex, WebRequest request) {

    ErrorApiResponse response = responseFactory.unauthorized(
        ex.getMessage(), getPath(request));

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  /**
   * Maneja excepciones de autorización (falta de permisos).
   */
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorApiResponse> handleForbidden(
      ForbiddenException ex, WebRequest request) {

    ErrorApiResponse response = responseFactory.forbidden(
        ex.getMessage(), getPath(request));

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  /**
   * Maneja excepciones de conflicto (ej: email duplicado).
   */
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorApiResponse> handleConflict(
      ConflictException ex, WebRequest request) {

    ErrorApiResponse response = responseFactory.conflict(
        ex.getMessage(), getPath(request));

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  /**
   * Maneja todas las excepciones no controladas.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorApiResponse> handleGeneric(
      Exception ex, WebRequest request) {

    ErrorApiResponse response = responseFactory.internalError(
        "Ha ocurrido un error inesperado en el servidor",
        getPath(request),
        ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
