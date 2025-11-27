package com.jonathanmarquez.security.email_verification.exception;

/**
 * Excepción lanzada cuando se alcanza el máximo de intentos de verificación.
 */
public class OtpMaxAttemptsException extends RuntimeException {

  public OtpMaxAttemptsException() {
    super("Has alcanzado el máximo de intentos permitidos. Por favor, solicita un nuevo código.");
  }

  public OtpMaxAttemptsException(String message) {
    super(message);
  }
}