/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.enums;

/**
 * Roles de conveniencia incluidos en la librería.
 *
 * <p>Implementan {@link RoleDefinition} por lo que pueden usarse directamente.
 * Son opcionales — el consumidor puede definir su propio enum con los roles
 * específicos de su dominio implementando {@link RoleDefinition}.</p>
 */
public enum Role implements RoleDefinition {

  ROLE_USER,
  ROLE_ADMIN;
  /**
   * Convierte una cadena al valor correspondiente de Role.
   *
   * @throws IllegalArgumentException si el rol no es válido
   */
  public static Role fromString(String role) {
    try {
      return Role.valueOf(role.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Rol no válido: " + role, e);
    }
  }
}