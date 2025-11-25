package com.jonathanmarquez.security.email_verification.exception;

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