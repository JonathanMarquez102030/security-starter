/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

public interface CustomErrorResponse {

  /**
   * Return the HTTP status to use for the response.
   */
  HttpStatus getStatus();
}
