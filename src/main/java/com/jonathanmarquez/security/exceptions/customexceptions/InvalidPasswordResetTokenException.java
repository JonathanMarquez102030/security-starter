package com.jonathanmarquez.security.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

public class InvalidPasswordResetTokenException extends RuntimeException implements CustomErrorResponse {

  private static final String DEFAULT_MESSAGE = "Password reset token inválido o expirado";

  public InvalidPasswordResetTokenException() {
    super(DEFAULT_MESSAGE);
  }

  public InvalidPasswordResetTokenException(String message) {
    super(message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }
}