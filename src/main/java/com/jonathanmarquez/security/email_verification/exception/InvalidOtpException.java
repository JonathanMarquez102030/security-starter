package com.jonathanmarquez.security.email_verification.exception;

/**
 * Excepción lanzada cuando el código OTP es inválido.
 */
public class InvalidOtpException extends RuntimeException {

  public InvalidOtpException() {
    super("El código OTP es inválido.");
  }

  public InvalidOtpException(String message) {
    super(message);
  }
}