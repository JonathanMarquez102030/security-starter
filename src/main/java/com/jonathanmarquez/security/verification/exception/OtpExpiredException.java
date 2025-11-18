package com.jonathanmarquez.security.verification.exception;

/**
 * Excepción lanzada cuando un token OTP ha expirado.
 */
public class OtpExpiredException extends RuntimeException {
  
  public OtpExpiredException() {
    super("El código OTP ha expirado. Por favor, solicita uno nuevo.");
  }

  public OtpExpiredException(String message) {
    super(message);
  }
}