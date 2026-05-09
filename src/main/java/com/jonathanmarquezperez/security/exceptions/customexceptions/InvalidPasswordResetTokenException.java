/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.exceptions.customexceptions;

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
