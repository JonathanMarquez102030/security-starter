/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.email_verification.exception;

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
