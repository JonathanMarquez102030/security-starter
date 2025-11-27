package com.jonathanmarquez.security.security.enums;

import lombok.Getter;

@Getter
public enum Role {
  // Roles base del sistema
  ROLE_USER("USERS", "Usuario estándar del sistema"),
  ROLE_ADMIN("ADMINS", "Administrador del sistema"),
  ROLE_MANAGER("MANAGERS", "Gerente con acceso a funciones avanzadas");

  // Obtener el nombre del grupo asociado al rol
  private final String groupName;
  // Obtener la descripción del rol
  private final String description;

  Role(String groupName, String description) {
    this.groupName = groupName;
    this.description = description;
  }

  // Convertir de String a Role (útil para deserialización)
  public static Role fromString(String role) {
    try {
      return Role.valueOf(role.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Rol no válido: " + role, e);
    }
  }

  // Obtener el rol a partir del nombre del grupo
  public static Role fromGroupName(String groupName) {
    for (Role role : values()) {
      if (role.groupName.equalsIgnoreCase(groupName)) {
        return role;
      }
    }
    throw new IllegalArgumentException("No se encontró un rol para el grupo: " + groupName);
  }

  // Obtener el nombre del rol (sin el prefijo ROLE_)
  public String getSimpleName() {
    return this.name().replace("ROLE_", "");
  }
}