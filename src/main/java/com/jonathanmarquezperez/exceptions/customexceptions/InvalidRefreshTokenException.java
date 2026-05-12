/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción que indica que el refresh token proporcionado es inválido o ha expirado.
 *
 * <p>Esta excepción se lanza cuando se intenta renovar un access token utilizando
 * un refresh token que no es válido, ha expirado o ha sido revocado.</p>
 *
 * @author Jonathan Marquez
 * @version 1.0
 * @since 2025-01
 */
public class InvalidRefreshTokenException extends RuntimeException implements CustomErrorResponse {

  private static final String DEFAULT_MESSAGE = "Refresh token inválido o expirado";

  /**
   * Construye una nueva excepción con el mensaje por defecto.
   */
  public InvalidRefreshTokenException() {
    super(DEFAULT_MESSAGE);
  }

  /**
   * Construye una nueva excepción con el mensaje especificado.
   *
   * @param message mensaje de error personalizado
   */
  public InvalidRefreshTokenException(String message) {
    super(message);
  }

  /**
   * Construye una nueva excepción con el mensaje y causa especificados.
   *
   * @param message mensaje de error personalizado
   * @param cause   causa raíz de la excepción
   */
  public InvalidRefreshTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Construye una nueva excepción con el mensaje por defecto y la causa especificada.
   *
   * @param cause causa raíz de la excepción
   */
  public InvalidRefreshTokenException(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
  }

  /**
   * Retorna el estado HTTP apropiado para esta excepción.
   *
   * @return HttpStatus.UNAUTHORIZED (401) indicando falta de autenticación válida
   */
  @Override
  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }
}
