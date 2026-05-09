/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.email_verification.exception;

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