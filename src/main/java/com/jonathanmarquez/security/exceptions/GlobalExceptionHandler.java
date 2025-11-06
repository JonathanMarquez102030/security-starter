package com.jonathanmarquez.security.exceptions;

import com.jonathanmarquez.security.exceptions.customexceptions.EmailAddressAlreadyExistsException;
import com.jonathanmarquez.security.exceptions.customexceptions.UserNotAuthenticatedException;
import com.jonathanmarquez.security.exceptions.customexceptions.UserNotFoundException;
import com.jonathanmarquez.security.exceptions.response.ErrorApiResponse;
import com.jonathanmarquez.security.utils.ProfileDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la aplicación.
 *
 * <p>Esta clase captura y maneja excepciones lanzadas en los controladores, devolviendo respuestas
 * HTTP estandarizadas con detalles del error.
 *
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private final ProfileDetector profileDetector;


  /**
   * Maneja todas las excepciones internas de Spring Framework.
   *
   * <p>Intercepta excepciones como:
   * - HttpRequestMethodNotSupportedException (405)
   * - HttpMediaTypeNotSupportedException (415)
   * - MissingServletRequestParameterException (400)
   * - TypeMismatchException (400)
   * - Y muchas otras excepciones estándar de Spring
   *
   * @param ex         la excepción lanzada
   * @param body       el cuerpo de respuesta (normalmente null)
   * @param headers    headers HTTP de la respuesta
   * @param statusCode código de estado HTTP determinado por Spring
   * @param request    el request web actual
   * @return ResponseEntity con formato estandarizado
   */
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      @Nullable Object body,
      @NonNull HttpHeaders headers,
      HttpStatusCode statusCode,
      @NonNull WebRequest request) {

    log.warn("Spring internal exception: {} - Status: {}",
             ex.getClass().getSimpleName(),
             statusCode.value());

    if (HttpStatus.INTERNAL_SERVER_ERROR.equals(statusCode)) {
      request.setAttribute("jakarta.servlet.error.exception", ex, 0);
      log.error("Internal server error", ex);
    }

    HttpStatus httpStatus = HttpStatus.valueOf(statusCode.value());

    ErrorApiResponse response = createErrorResponse(
        ex,
        httpStatus,
        buildUserFriendlyMessage(ex, httpStatus),
        getPath(request),
        buildDetails(ex, null)
    );

    return new ResponseEntity<>(response, headers, httpStatus);
  }

  /**
   * Maneja todas las excepciones personalizadas planteadas dentro del proyecto o modificar las existentes por Spring.
   *
   * @param ex      la excepción a manejar
   * @param request la solicitud actual
   */
  @ExceptionHandler({
      EmailAddressAlreadyExistsException.class,
      UserNotAuthenticatedException.class,
      UserNotFoundException.class,
  })
  @Nullable
  public final ResponseEntity<ErrorApiResponse> handleCustomException(
      Exception ex, WebRequest request) {

    return switch (ex) {
      case EmailAddressAlreadyExistsException e -> handleEmailAddressAlreadyExistsException(e, request);
      case UserNotAuthenticatedException e -> handleUserNotAuthenticatedException(e, request);
      case UserNotFoundException e -> handleUserNotFoundException(e, request);
      default -> handleGeneric(ex, request);
    };
  }

  // ================================================================================================================
  // Métodos específicos para cada tipo de excepción custom.
  // ================================================================================================================

  /**
   * Maneja excepción de email ya existente.
   */
  @Nullable
  protected ResponseEntity<ErrorApiResponse> handleEmailAddressAlreadyExistsException(
      EmailAddressAlreadyExistsException ex, WebRequest request) {

    ErrorApiResponse response = createErrorResponse(
        ex,
        ex.getStatus(),
        ex.getMessage(),
        getPath(request),
        buildDetails(ex, null)
    );

    return ResponseEntity.status(ex.getStatus()).body(response);
  }

  /**
   * Maneja excepción de usuario no autenticado.
   */
  @Nullable
  protected ResponseEntity<ErrorApiResponse> handleUserNotAuthenticatedException(
      UserNotAuthenticatedException ex, WebRequest request) {

    String details = buildDetails(ex, "Authentication failed: " + ex.getMessage());

    ErrorApiResponse response = createErrorResponse(
        ex,
        ex.getStatus(),
        ex.getMessage(),
        getPath(request),
        details
    );

    return ResponseEntity.status(ex.getStatus()).body(response);
  }

  /**
   * Maneja excepción de usuario no encontrado.
   */
  @Nullable
  protected ResponseEntity<ErrorApiResponse> handleUserNotFoundException(
      UserNotFoundException ex, WebRequest request) {

    ErrorApiResponse response = createErrorResponse(
        ex,
        ex.getStatus(),
        ex.getMessage(),
        getPath(request),
        buildDetails(ex,null)
    );

    return ResponseEntity.status(ex.getStatus()).body(response);
  }


  // ================================================================================================================
  // Métodos específicos para cada tipo de excepción de Spring personalizada.
  // ================================================================================================================

  /**
   * Maneja errores de validación de Spring (@Valid).
   * Similar a handleMethodArgumentNotValid de Spring.
   */
  protected ResponseEntity<ErrorApiResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, WebRequest request) {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    String errorDetails = ex.getBindingResult()
                            .getFieldErrors()
                            .stream()
                            .map(error -> error.getField() + ": " + error.getDefaultMessage())
                            .collect(Collectors.joining(", "));

    ErrorApiResponse response = createErrorResponse(
        ex,
        status,
        buildUserFriendlyMessage(ex, status),
        getPath(request),
        errorDetails
    );

    return ResponseEntity.status(status).body(response);
  }

  /**
   * Maneja todas las excepciones no controladas.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorApiResponse> handleGeneric(
      Exception ex, WebRequest request) {

    log.error("Unhandled exception", ex);

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String details = buildDetails(ex, String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage()));

    ErrorApiResponse response = createErrorResponse(
        ex,
        status,
        ex.getMessage(),
        getPath(request),
        details
    );

    return ResponseEntity.status(status).body(response);
  }


  // ================================================================================================================
  // MÉTODOS AUXILIARES
  // ================================================================================================================

  /**
   * Función central para crear respuestas de error estandarizadas.
   * Similar al patrón usado internamente por Spring en ResponseEntityExceptionHandler.
   *
   * @param ex      la excepción
   * @param status  el código de estado HTTP
   * @param message mensaje para el usuario
   * @param path    la ruta del request
   * @param details detalles adicionales (opcional)
   * @return ErrorApiResponse construida
   */
  private ErrorApiResponse createErrorResponse(
      Exception ex,
      HttpStatus status,
      String message,
      String path,
      String details) {

    return ErrorApiResponse.builder()
                           .success(false)
                           .message(message)
                           .status(status.getReasonPhrase().toLowerCase())
                           .statusCode(status.value())
                           .timestamp(LocalDateTime.now())
                           .path(path)
                           .details(details)
                           .build();
  }

  /**
   * Extrae el path limpio del WebRequest.
   */
  private String getPath(WebRequest request) {
    return request.getDescription(false).replace("uri=", "");
  }


  /**
   * Construye un mensaje amigable para el usuario según el tipo de excepción.
   *
   * <p>Función utilitaría para excepciones de spring.</p>
   *
   * @param ex     la excepción
   * @param status el estado HTTP
   * @return mensaje descriptivo
   */
  private String buildUserFriendlyMessage(Exception ex, HttpStatus status) {

    // Mapeo de excepciones a mensajes amigables
    return switch (ex) {
      case HttpRequestMethodNotSupportedException e ->
          "Método HTTP no permitido para este endpoint";

      case HttpMediaTypeNotSupportedException e ->
          "Tipo de contenido no soportado. Use 'application/json'";

      case HttpMediaTypeNotAcceptableException e ->
          "El servidor no puede generar una respuesta en el formato solicitado";

      case MissingPathVariableException e ->
          "Falta una variable de ruta requerida";

      case MissingServletRequestParameterException e ->
          "Faltan parámetros requeridos en la solicitud";

      case MissingServletRequestPartException e ->
          "Falta una parte requerida en la solicitud multipart";

      case ServletRequestBindingException e ->
          "Error al vincular los parámetros de la solicitud";

      case MethodArgumentNotValidException e ->
          "Error de validación en los datos enviados";

      case HandlerMethodValidationException e ->
          "Error de validación en los parámetros del método";

      case NoHandlerFoundException e ->
          "Endpoint no encontrado";

      case NoResourceFoundException e ->
          "Recurso no encontrado";

      case AsyncRequestTimeoutException e ->
          "La solicitud asíncrona ha excedido el tiempo de espera";

      case ErrorResponseException e ->
          "Error en la respuesta del servidor";

      case MaxUploadSizeExceededException e ->
          "El archivo excede el tamaño máximo permitido";

      case ConversionNotSupportedException e ->
          "Error interno: conversión de tipo no soportada";

      case TypeMismatchException e ->
          "Tipo de dato inválido en los parámetros";

      case HttpMessageNotReadableException e ->
          "El cuerpo de la solicitud no es válido o está mal formado";

      case HttpMessageNotWritableException e ->
          "Error interno al escribir la respuesta";

      case MethodValidationException e ->
          "Error de validación en el método";

      case AsyncRequestNotUsableException e ->
          "La solicitud asíncrona ya no es utilizable";

      default -> {
        // Si es 4xx, mensaje genérico para cliente
        if (status.is4xxClientError()) {
          yield "Error en la solicitud del cliente";
        }
        // Sí es 5xx, mensaje genérico para servidor
        yield "Error interno del servidor";
      }
    };
  }


  /**
   * Construye detalles técnicos con contexto adicional.
   *
   * @param ex                la excepción
   * @param additionalContext contexto adicional
   * @return detalles técnicos o null si estamos en producción
   */
  private String buildDetails(Exception ex, @Nullable String additionalContext) {

    if (profileDetector.isProfileActive("prod")) {
      return null;
    }

    StringBuilder details = new StringBuilder();

    if (additionalContext != null) {
      details.append(additionalContext);
    } else if (ex.getMessage() != null) {
      details.append(ex.getMessage());
    }

    if (details.isEmpty()) {
      details.append(ex.getClass().getSimpleName());
    }

    return details.toString();
  }
}
