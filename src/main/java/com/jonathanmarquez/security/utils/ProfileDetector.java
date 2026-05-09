/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.utils;

import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilidad para detectar y verificar perfiles de Spring activos en la aplicación.
 *
 * <p>Esta clase proporciona procesos de ayuda para consultar qué perfiles de Spring
 * están activos durante la ejecución de la aplicación. Es útil para comportamiento
 * condicional basado en el entorno (desarrollo, producción, testing, etc.) y para
 * decisiones sobre logging, configuración de seguridad, o características específicas
 * del entorno.</p>
 */
public class ProfileDetector {

  private final Environment env;

  /**
   * Constructor que inyecta el objeto Environment de Spring.
   *
   * @param env objeto Environment para acceder a los perfiles activos
   */
  public ProfileDetector(Environment env) {
    this.env = env;
  }

  /**
   * Verifica si el perfil especificado está activo en la aplicación.
   *
   * <p>La comparación es insensible a mayúsculas y minúsculas.</p>
   *
   * @param profileName nombre del perfil a verificar
   * @return true si el perfil está activo, false en caso contrario
   */
  public boolean isProfileActive(String profileName) {
    return Arrays.stream(env.getActiveProfiles())
                 .anyMatch(profile -> profile.equalsIgnoreCase(profileName));
  }

  /**
   * Verifica si al menos uno de los perfiles especificados está activo.
   *
   * <p>La comparación es insensible a mayúsculas y minúsculas.</p>
   *
   * @param profileNames nombres de los perfiles a verificar
   * @return true si al menos uno de los perfiles está activo, false si ninguno lo está
   */
  @SuppressWarnings("unused")
  public boolean isAnyProfileActive(String... profileNames) {
    Set<String> targetProfiles = Arrays.stream(profileNames)
                                       .map(String::toLowerCase)
                                       .collect(Collectors.toSet());

    return Arrays.stream(env.getActiveProfiles())
                 .map(String::toLowerCase)
                 .anyMatch(targetProfiles::contains);
  }

  /**
   * Verifica si todos los perfiles especificados están activos simultáneamente.
   *
   * <p>La comparación es insensible a mayúsculas y minúsculas.</p>
   *
   * @param profileNames nombres de los perfiles a verificar
   * @return true si todos los perfiles están activos, false si falta alguno
   */
  @SuppressWarnings("unused")
  public boolean areAllProfilesActive(String... profileNames) {
    Set<String> targetProfiles = Arrays.stream(profileNames)
                                       .map(String::toLowerCase)
                                       .collect(Collectors.toSet());

    Set<String> activeProfiles = Arrays.stream(env.getActiveProfiles())
                                       .map(String::toLowerCase)
                                       .collect(Collectors.toSet());

    return activeProfiles.containsAll(targetProfiles);
  }

  /**
   * Obtiene la lista de todos los perfiles activos en la aplicación.
   *
   * @return lista con los nombres de los perfiles activos
   */
  @SuppressWarnings("unused")
  public List<String> getActiveProfiles() {
    return Arrays.asList(env.getActiveProfiles());
  }
}