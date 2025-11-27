package com.jonathanmarquez.security.email_verification.exception;

/**
 * Excepción lanzada cuando no se encuentra un OTP para el email especificado.
 */
public class OtpNotFoundException extends RuntimeException {

  public OtpNotFoundException() {
    super("No se encontró ningún código OTP activo para este email.");
  }

  public OtpNotFoundException(String message) {
    super(message);
  }
}