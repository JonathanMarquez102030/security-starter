package com.jonathanmarquez.security.security.enums;

import lombok.Getter;

/**
 * Enumeración que define los roles de usuario disponibles en el sistema.
 *
 * <p>Esta enumeración gestiona los diferentes niveles de acceso y permisos dentro de la aplicación.
 * Cada rol tiene asociado un nombre de grupo para gestión de permisos y una descripción que
 * explica sus capacidades. Los roles siguen la convención de Spring Security con el prefijo ROLE_.</p>
 */
@Getter
public enum Role {

  // Roles base del sistema
  ROLE_USER("USERS", "Usuario estándar del sistema"),
  ROLE_ADMIN("ADMINS", "Administrador del sistema"),
  ROLE_MANAGER("MANAGERS", "Gerente con acceso a funciones avanzadas");

  /**
   * Nombre del grupo asociado al rol para gestión de permisos.
   */
  private final String groupName;
  /**
   * Descripción detallada del rol y sus capacidades.
   */
  private final String description;

  /**
   * Constructor privado para inicializar los valores del rol.
   *
   * @param groupName nombre del grupo asociado al rol
   * @param description descripción del rol y sus permisos
   */
  Role(String groupName, String description) {
    this.groupName = groupName;
    this.description = description;
  }

  /**
   * Convierte una cadena de texto a su correspondiente valor de Role.
   *
   * <p>Este proceso es útil durante la deserialización o cuando se reciben roles como cadenas
   * de texto desde fuentes externas. La conversión es insensible a mayúsculas/minúsculas.</p>
   *
   * @param role cadena de texto que representa el rol
   * @return el valor de Role correspondiente
   * @throws IllegalArgumentException si el rol proporcionado no es válido
   */
  public static Role fromString(String role) {
    try {
      return Role.valueOf(role.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Rol no válido: " + role, e);
    }
  }

  /**
   * Obtiene el rol correspondiente a partir del nombre de su grupo.
   *
   * <p>Busca entre todos los roles definidos aquel cuyo nombre de grupo coincida con el
   * proporcionado. La búsqueda es insensible a mayúsculas/minúsculas.</p>
   *
   * @param groupName nombre del grupo del cual obtener el rol
   * @return el Role asociado al nombre de grupo proporcionado
   * @throws IllegalArgumentException si no se encuentra ningún rol con ese nombre de grupo
   */
  public static Role fromGroupName(String groupName) {
    for (Role role : values()) {
      if (role.groupName.equalsIgnoreCase(groupName)) {
        return role;
      }
    }
    throw new IllegalArgumentException("No se encontró un rol para el grupo: " + groupName);
  }

  /**
   * Obtiene el nombre simplificado del rol sin el prefijo ROLE_.
   *
   * <p>Por ejemplo, ROLE_ADMIN se convierte en ADMIN, ROLE_USER en USER, etc.
   * Útil para presentación en interfaces de usuario o logs.</p>
   *
   * @return el nombre del rol sin el prefijo ROLE_
   */
  public String getSimpleName() {
    return this.name().replace("ROLE_", "");
  }
}