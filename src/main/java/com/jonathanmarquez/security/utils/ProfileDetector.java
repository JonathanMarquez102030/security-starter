package com.jonathanmarquez.security.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProfileDetector {

  private final Environment env;

  @Autowired
  public ProfileDetector(Environment env) {
    this.env = env;
  }

  /**
   * Verifica si el perfil especificado está activo
   */
  public boolean isProfileActive(String profileName) {
    return Arrays.stream(env.getActiveProfiles())
                 .anyMatch(profile -> profile.equalsIgnoreCase(profileName));
  }

  /**
   * Verifica si alguno de los perfiles especificados está activo
   */
  public boolean isAnyProfileActive(String... profileNames) {
    Set<String> targetProfiles = Arrays.stream(profileNames)
                                       .map(String::toLowerCase)
                                       .collect(Collectors.toSet());

    return Arrays.stream(env.getActiveProfiles())
                 .map(String::toLowerCase)
                 .anyMatch(targetProfiles::contains);
  }

  /**
   * Verifica si todos los perfiles especificados están activos
   */
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
   * Obtiene todos los perfiles activos como lista
   */
  public List<String> getActiveProfiles() {
    return Arrays.asList(env.getActiveProfiles());
  }
}