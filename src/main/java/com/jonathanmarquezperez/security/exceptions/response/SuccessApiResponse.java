/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.exceptions.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Clase genérica para estandarizar todas las respuestas de la API REST.
 * Proporciona una estructura consistente que incluye el estado de la operación,
 * mensaje descriptivo, datos de respuesta y metadatos adicionales.
 *
 * <p>Esta clase utiliza generics para permitir diferentes tipos de datos
 * en la respuesta, manteniendo la flexibilidad mientras garantiza la consistencia.</p>
 *
 * @param <T> el tipo de datos que contendrá la respuesta
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class SuccessApiResponse<T> extends ApiResponse<T> {

  private T data;
}
