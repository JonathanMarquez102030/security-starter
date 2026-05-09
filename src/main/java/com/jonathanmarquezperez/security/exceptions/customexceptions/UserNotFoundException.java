/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción que indica que un producto no ha sido encontrado.
 *
 * <p>Esta excepción se lanza cuando se intenta acceder a un producto que no existe en la base de
 * datos.
 */
public class UserNotFoundException extends RuntimeException implements CustomErrorResponse {


  private static final String DEFAULT_MESSAGE = "Usuario no encontrado";

  public UserNotFoundException() {
    super(DEFAULT_MESSAGE);
  }

  public UserNotFoundException(String message) {
    super(message);
  }

  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNotFoundException(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
