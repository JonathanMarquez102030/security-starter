/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

public class UserNotAuthenticatedException extends RuntimeException implements CustomErrorResponse {
  private static final String DEFAULT_MESSAGE = "El usuario no esta autenticado";

  public UserNotAuthenticatedException() {
    super(DEFAULT_MESSAGE);
  }

  public UserNotAuthenticatedException(String message) {
    super(message);
  }

  public UserNotAuthenticatedException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNotAuthenticatedException(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }
}
