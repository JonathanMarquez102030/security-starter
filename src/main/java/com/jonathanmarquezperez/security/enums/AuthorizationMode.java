/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.enums;

/**
 * Define el modo de autorización a utilizar en la aplicación.
 */
public enum AuthorizationMode {
  /**
   * Usa la tabla 'authorities' directamente.
   */
  AUTHORITIES,

  /**
   * Usa las tablas 'groups', 'group_authorities' y 'group_members'.
   */
  GROUPS
}
