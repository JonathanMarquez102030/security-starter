/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.email_verification.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Excepción lanzada cuando un usuario intenta autenticarse sin haber verificado su correo electrónico.
 */
public class EmailNotVerifiedException extends AuthenticationException {

  public EmailNotVerifiedException() {
    super("Email no verificado. Por favor, verifica tu correo electrónico antes de iniciar sesión.");
  }

  public EmailNotVerifiedException(String message) {
    super(message);
  }

  public EmailNotVerifiedException(String message, Throwable cause) {
    super(message, cause);
  }
}
