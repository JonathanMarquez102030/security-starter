/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.jpa;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Enum que representa los posibles estados de una entidad en el sistema.
 *
 * @version 1.0
 */
@Getter
@NoArgsConstructor
public enum EntityStatus {
  INACTIVE,
  ACTIVE
}
