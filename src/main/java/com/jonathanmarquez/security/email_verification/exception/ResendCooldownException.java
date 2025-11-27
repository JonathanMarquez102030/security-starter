package com.jonathanmarquez.security.email_verification.exception;

/**
 * Excepción lanzada cuando se intenta reenviar un OTP antes del cooldown.
 */
public class ResendCooldownException extends RuntimeException {

  public ResendCooldownException(long secondsRemaining) {
    super(String.format("Debes esperar %d segundos antes de solicitar un nuevo código.", secondsRemaining));
  }

  public ResendCooldownException(String message) {
    super(message);
  }
}