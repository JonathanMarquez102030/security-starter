/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.security.enums;

import lombok.Getter;

/**
 * Enum que define los perfiles (profiles) disponibles en la aplicación Spring Boot.
 * <p>
 * Estos perfiles permiten configurar diferentes comportamientos según el entorno
 * de ejecución (desarrollo, pruebas, producción).
 * </p>
 *
 */
@Getter
public enum SpringProfile {

  /**
   * Perfil de desarrollo local.
   * Usado durante el desarrollo con configuraciones permisivas y herramientas de debug.
   */
  DEV("dev"),

  /**
   * Perfil de producción.
   * Configuración optimizada y segura para el entorno productivo.
   */
  PROD("prod");

  private final String profileName;

  SpringProfile(String profileName) {
    this.profileName = profileName;
  }
}
