/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

public class EmailAddressAlreadyExistsException extends RuntimeException implements CustomErrorResponse {
  private static final String DEFAULT_MESSAGE = "Ya existe una cuenta con ese correo";

  public EmailAddressAlreadyExistsException() {
    super(DEFAULT_MESSAGE);
  }

  public EmailAddressAlreadyExistsException(String message) {
    super(message);
  }

  public EmailAddressAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmailAddressAlreadyExistsException(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }
}
