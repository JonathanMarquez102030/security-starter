package com.jonathanmarquez.security.utils;

/**
 * Constantes que contienen mensajes estandarizados para las respuestas de la API.
 * Centraliza todos los mensajes para facilitar la traducción, mantenimiento
 * y consistencia en toda la aplicación.
 *
 * <p>Los mensajes están organizados por categorías funcionales y utilizan
 * nomenclatura descriptiva para facilitar su identificación y uso.</p>
 *
 */
public final class ResponseMessages {

  /**
   * Constructor privado para prevenir instanciación.
   * Esta clase solo contiene constantes estáticas.
   */
  private ResponseMessages() {
    throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
  }

  // ================================
  // MENSAJES GENERALES
  // ================================


  public static final String OPERATION_SUCCESSFUL = "Operación realizada exitosamente";

  public static final String OPERATION_FAILED = "La operación no pudo ser completada";

  public static final String RESOURCE_NOT_FOUND = "El recurso solicitado no fue encontrado";

  public static final String UNAUTHORIZED_ACCESS = "No tiene permisos para acceder a este recurso";

  public static final String VALIDATION_ERROR = "Los datos proporcionados no son válidos";

  // ================================
  // MENSAJES DE OPERACIONES CRUD
  // ================================


  public static final String RESOURCE_CREATED = "Recurso creado exitosamente";

  public static final String RESOURCE_RETRIEVED = "Recurso obtenido exitosamente";

  public static final String RESOURCE_UPDATED = "Recurso actualizado exitosamente";

  public static final String RESOURCE_DELETED = "Recurso eliminado exitosamente";

  public static final String RESOURCES_RETRIEVED = "Recursos obtenidos exitosamente";

  // ================================
  // MENSAJES DE AUTENTICACIÓN
  // ================================


  public static final String LOGIN_SUCCESSFUL = "Inicio de sesión exitoso";

  public static final String LOGOUT_SUCCESSFUL = "Cierre de sesión exitoso";

  public static final String INVALID_CREDENTIALS = "Las credenciales proporcionadas son inválidas";

  public static final String TOKEN_EXPIRED = "El token de sesión ha expirado";

  public static final String INVALID_TOKEN = "El token proporcionado es inválido";

  public static final String TOKEN_REFRESHED = "Token renovado exitosamente";

  public static final String EMAIL_VERIFIED = "Email verificado exitosamente";

  // ================================
  // MENSAJES DE PASSWORD / OTP
  // ================================

  public static final String OTP_SENT = "Código OTP enviado exitosamente";
  public static final String OTP_RESENT = "Código OTP reenviado exitosamente";

  public static final String PASSWORD_FORGOT_OTP_SENT_GENERIC =
      "Si el correo existe en nuestro sistema, enviaremos un código OTP para restablecer la contraseña";

  public static final String PASSWORD_RESET_TOKEN_ISSUED =
      "Token de restablecimiento generado";

  public static final String PASSWORD_RESET_SUCCESS =
      "Contraseña actualizada exitosamente";

  public static final String PASSWORD_CHANGE_OTP_SENT =
      "Código OTP enviado para confirmar el cambio de contraseña";

  public static final String PASSWORD_CHANGED_SUCCESS =
      "Contraseña cambiada exitosamente";

  // ================================
  // MENSAJES DE USUARIOS
  // ================================


  public static final String USER_CREATED = "Usuario creado exitosamente";

  public static final String USER_RETRIEVED = "Usuario obtenido exitosamente";

  public static final String USER_UPDATED = "Usuario actualizado exitosamente";

  public static final String USER_DELETED = "Usuario eliminado exitosamente";

  public static final String USERS_RETRIEVED = "Usuarios obtenidos exitosamente";


  // ================================
  // MENSAJES DE ERROR TÉCNICO
  // ================================


  public static final String INTERNAL_SERVER_ERROR = "Error interno del servidor";

  public static final String DATABASE_ERROR = "Error en la base de datos";

  public static final String EXTERNAL_SERVICE_ERROR = "Error en servicio externo";

  public static final String CONNECTION_ERROR = "Error de conexión";

  // ================================
  // CÓDIGOS DE ERROR
  // ================================


  public static final String ERROR_CODE_VALIDATION = "ERR_VALIDATION";

  public static final String ERROR_CODE_NOT_FOUND = "ERR_NOT_FOUND";

  public static final String ERROR_CODE_UNAUTHORIZED = "ERR_UNAUTHORIZED";

  public static final String ERROR_CODE_INTERNAL = "ERR_INTERNAL";

  public static final String ERROR_CODE_DATABASE = "ERR_DATABASE";
}