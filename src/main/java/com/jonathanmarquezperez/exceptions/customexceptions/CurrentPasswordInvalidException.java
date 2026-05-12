/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

public class CurrentPasswordInvalidException extends RuntimeException implements CustomErrorResponse {

  public CurrentPasswordInvalidException() {
    super("La contraseña actual es incorrecta");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}
