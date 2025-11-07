package com.jonathanmarquez.security.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jonathanmarquez.security.exceptions.customexceptions.EmailAddressAlreadyExistsException;
import com.jonathanmarquez.security.exceptions.customexceptions.UserNotAuthenticatedException;
import com.jonathanmarquez.security.exceptions.customexceptions.UserNotFoundException;
import com.jonathanmarquez.security.exceptions.helpers.ErrorApiResponseHelper;
import com.jonathanmarquez.security.exceptions.response.ErrorApiResponse;
import com.jonathanmarquez.security.utils.ProfileDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransactionException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.time.LocalDateTime;
import java.util.concurrent.RejectedExecutionException;
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
        ErrorApiResponseHelper.buildDetails(profileDetector, ex, null)
    );

    return new ResponseEntity<>(response, headers, httpStatus);
  }

  /**
   * Maneja todas las excepciones no controladas.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorApiResponse> handleGeneric(
      Exception ex, WebRequest request) {

    log.error("Unhandled exception", ex);

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String details = ErrorApiResponseHelper.buildDetails(profileDetector, ex,
                                                         String.format("%s: %s", ex.getClass().getSimpleName(),
                                                                       ex.getMessage()));

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
  // Métodos específicos para cada tipo de excepción custom.
  // ================================================================================================================

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
        ErrorApiResponseHelper.buildDetails(profileDetector, ex, null)
    );

    return ResponseEntity.status(ex.getStatus()).body(response);
  }

  /**
   * Maneja excepción de usuario no autenticado.
   */
  @Nullable
  protected ResponseEntity<ErrorApiResponse> handleUserNotAuthenticatedException(
      UserNotAuthenticatedException ex, WebRequest request) {

    String details = ErrorApiResponseHelper.buildDetails(profileDetector, ex,
                                                         "Authentication failed: " + ex.getMessage());

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
        ErrorApiResponseHelper.buildDetails(profileDetector, ex, null)
    );

    return ResponseEntity.status(ex.getStatus()).body(response);
  }


  // ================================================================================================================
  // Métodos específicos para excepciones de base de datos
  // ================================================================================================================

  /**
   * Maneja todas las excepciones relacionadas con base de datos, SQL y JPA.
   */
  @ExceptionHandler({
      InvalidDataAccessResourceUsageException.class,
      BadSqlGrammarException.class,
      SQLGrammarException.class,
      JpaSystemException.class,
      JpaObjectRetrievalFailureException.class,
      DataIntegrityViolationException.class,
      DataAccessException.class
  })
  @Nullable
  public final ResponseEntity<ErrorApiResponse> handleDatabaseException(
      Exception ex, WebRequest request) {

    return switch (ex) {
      case BadSqlGrammarException e -> handleSqlGrammarError(e, request);
      case InvalidDataAccessResourceUsageException e -> handleSqlGrammarError(e, request);
      case SQLGrammarException e -> handleSqlGrammarError(e, request);
      case JpaSystemException e -> handleJpaSystemError(e, request);
      case JpaObjectRetrievalFailureException e -> handleJpaSystemError(e, request);
      case DataIntegrityViolationException e -> handleDataIntegrityError(e, request);
      case DataAccessException e -> handleGenericDatabaseError(e, request);
      default -> handleGeneric(ex, request);
    };
  }

  /**
   * Maneja errores de SQL Grammar.
   */
  @Nullable
  protected ResponseEntity<ErrorApiResponse> handleSqlGrammarError(
      Exception ex, WebRequest request) {

    log.error("SQL Grammar error: {}", ex.getMessage(), ex);

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String cleanedSqlError = extractSqlErrorMessage(ex);

    ErrorApiResponse response = createErrorResponse(
        ex,
        status,
        buildUserFriendlyMessage(ex, status),
        getPath(request),
        cleanedSqlError
    );

    return ResponseEntity.status(status).body(response);
  }

  /**
   * Maneja errores de JPA System.
   */
  @Nullable
  protected ResponseEntity<ErrorApiResponse> handleJpaSystemError(
      Exception ex, WebRequest request) {

    log.error("JPA System error: {}", ex.getMessage(), ex);

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String cleanedError = extractJpaErrorMessage(ex);

    ErrorApiResponse response = createErrorResponse(
        ex,
        status,
        buildUserFriendlyMessage(ex, status),
        getPath(request),
        cleanedError
    );

    return ResponseEntity.status(status).body(response);
  }

  /**
   * Maneja errores de integridad de datos.
   */
  @Nullable
  protected ResponseEntity<ErrorApiResponse> handleDataIntegrityError(
      DataIntegrityViolationException ex, WebRequest request) {

    log.error("Data integrity error: {}", ex.getMessage(), ex);

    HttpStatus status = HttpStatus.CONFLICT;

    ErrorApiResponse response = createErrorResponse(
        ex,
        status,
        buildUserFriendlyMessage(ex, status),
        getPath(request),
        ErrorApiResponseHelper.buildDetails(profileDetector, ex, null)
    );

    return ResponseEntity.status(status).body(response);
  }

  /**
   * Maneja errores genéricos de acceso a datos.
   */
  @Nullable
  protected ResponseEntity<ErrorApiResponse> handleGenericDatabaseError(
      DataAccessException ex, WebRequest request) {

    log.error("Database access error: {}", ex.getMessage(), ex);

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    ErrorApiResponse response = createErrorResponse(
        ex,
        status,
        buildUserFriendlyMessage(ex, status),
        getPath(request),
        ErrorApiResponseHelper.buildDetails(profileDetector, ex, null)
    );

    return ResponseEntity.status(status).body(response);
  }

  // ================================================================================================================
  // Métodos específicos para cada tipo de excepción de Spring personalizada.
  // ================================================================================================================

  /**
   * Maneja errores de validación de Spring (@Valid).
   * Similar a handleMethodArgumentNotValid de Spring.
   */
  @Override
  @Nullable
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                @Nullable HttpHeaders headers,
                                                                @Nullable HttpStatusCode statusCode,
                                                                @NonNull WebRequest request) {
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
      case HttpRequestMethodNotSupportedException e -> "Método HTTP no permitido para este endpoint";

      case HttpMediaTypeNotSupportedException e -> "Tipo de contenido no soportado. Use 'application/json'";

      case HttpMediaTypeNotAcceptableException e ->
          "El servidor no puede generar una respuesta en el formato solicitado";

      case MissingPathVariableException e -> "Falta una variable de ruta requerida";

      case MissingServletRequestParameterException e -> "Faltan parámetros requeridos en la solicitud";

      case MissingServletRequestPartException e -> "Falta una parte requerida en la solicitud multipart";

      case ServletRequestBindingException e -> "Error al vincular los parámetros de la solicitud";

      case MethodArgumentNotValidException e -> "Error de validación en los datos enviados";

      case HandlerMethodValidationException e -> "Error de validación en los parámetros del método";

      case NoHandlerFoundException e -> "Endpoint no encontrado";

      case NoResourceFoundException e -> "Recurso no encontrado";

      case AsyncRequestTimeoutException e -> "La solicitud asíncrona ha excedido el tiempo de espera";

      case ErrorResponseException e -> "Error en la respuesta del servidor";

      case MaxUploadSizeExceededException e -> "El archivo excede el tamaño máximo permitido";

      case ConversionNotSupportedException e -> "Error interno: conversión de tipo no soportada";

      case TypeMismatchException e -> "Tipo de dato inválido en los parámetros";

      case HttpMessageNotReadableException e -> "El cuerpo de la solicitud no es válido o está mal formado";

      case HttpMessageNotWritableException e -> "Error interno al escribir la respuesta";

      case MethodValidationException e -> "Error de validación en el método";

      case AsyncRequestNotUsableException e -> "La solicitud asíncrona ya no es utilizable";

      // Excepciones de SQL y Base de Datos (DE MÁS ESPECÍFICA A MÁS GENERAL)
      case JpaSystemException e -> "Error en la configuración de persistencia de datos";

      case JpaObjectRetrievalFailureException e -> "No se pudo recuperar el objeto de la base de datos";

      // Excepciones de Hibernate
      case SQLGrammarException e -> "Error en la estructura de la consulta a la base de datos";

      // Excepciones de Base de Datos
      case BadSqlGrammarException e -> "Error en la estructura de la consulta a la base de datos";

      case InvalidDataAccessResourceUsageException e -> "Error en la configuración de la base de datos";

      case SQLSyntaxErrorException e -> "Error de sintaxis en la consulta SQL";

      case SQLException e -> "Error al ejecutar la operación en la base de datos";

      case DataIntegrityViolationException e ->
          "Violación de integridad de datos. El registro podría estar duplicado o referenciado";

      case DataAccessException e -> "Error al acceder a la base de datos";

      case ConstraintViolationException e -> "Violación de restricciones de validación";

      // Excepciones de Seguridad
      case AccessDeniedException e -> "No tiene permisos para acceder a este recurso";

      case AuthenticationException e -> "Credenciales inválidas o token expirado";

      // Excepciones de Negocio Comunes
      case IllegalArgumentException e -> "Argumento inválido en la solicitud";

      case IllegalStateException e -> "La operación no se puede realizar en el estado actual";

      case NullPointerException e -> "Error interno: valor nulo inesperado";

      // Excepciones de Transacciones
      case TransactionException e -> "Error en la transacción de base de datos";

      // Excepciones de Timeout
      case SocketTimeoutException e -> "Tiempo de espera agotado en la conexión";

      // Excepciones de JSON/Serialización
      case JsonProcessingException e -> "Error al procesar formato JSON";

      // Excepciones de Recursos
      case FileNotFoundException e -> "Archivo no encontrado";

      case IOException e -> "Error de entrada/salida al procesar el recurso";

      // Excepciones de Límites
      case RejectedExecutionException e -> "El servidor está sobrecargado. Intente nuevamente más tarde";

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
   * Extrae y limpia el mensaje de error SQL para hacerlo más legible.
   */
  private String extractSqlErrorMessage(Exception ex) {
    if (profileDetector.isProfileActive("prod")) {
      return null;
    }

    String message = ex.getMessage();
    if (message == null) {
      return ex.getClass().getSimpleName();
    }

    // Buscar el patrón "ERROR: ..." hasta "]"
    if (message.contains("ERROR:")) {
      int errorStart = message.indexOf("ERROR:");
      int errorEnd = message.indexOf("]", errorStart);

      if (errorStart != -1 && errorEnd != -1) {
        String sqlError = message.substring(errorStart, errorEnd);
        // Limpiar saltos de línea y espacios múltiples
        return sqlError.replaceAll("\\\\n", " ")
                       .replaceAll("\\s+", " ")
                       .trim();
      }
    }

    return message;
  }

  /**
   * Extrae y limpia el mensaje de error JPA para hacerlo más legible.
   */
  private String extractJpaErrorMessage(Exception ex) {
    if (profileDetector.isProfileActive("prod")) {
      return null;
    }

    String message = ex.getMessage();
    if (message == null) {
      return ex.getClass().getSimpleName();
    }

    // Para JpaSystemException, el mensaje técnico está después de ": "
    if (message.contains(": ")) {
      int colonIndex = message.indexOf(": ");
      if (colonIndex != -1 && colonIndex + 2 < message.length()) {
        return message.substring(colonIndex + 2).trim();
      }
    }

    return message;
  }
}
