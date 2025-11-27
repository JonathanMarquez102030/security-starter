package com.jonathanmarquez.security.security.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementación personalizada de UserDetails que combina
 * los datos de Spring Security (users) con el perfil extendido (user_profiles).
 */
@Getter
public class SecurityUserDetails implements UserDetails {

  private final String username;
  private final String password;
  private final boolean enabled;
  private final Collection<? extends GrantedAuthority> authorities;

  // Datos extendidos del perfil
  private final UserProfile profile;

  /**
   * Constructor que combina usuario de Spring Security con perfil extendido.
   */
  public SecurityUserDetails(
      String username,
      String password,
      boolean enabled,
      Collection<String> authorities,
      UserProfile profile) {

    this.username = username;
    this.password = password;
    this.enabled = enabled;
    this.authorities = authorities.stream()
                                  .map(SimpleGrantedAuthority::new)
                                  .collect(Collectors.toSet());
    this.profile = profile;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  // Métodos de conveniencia para acceder al perfil
  public String getEmail() {
    return profile != null ? profile.getEmail() : null;
  }

  public String getFullName() {
    if (profile == null) return username;
    return String.format("%s %s",
                         profile.getFirstName() != null ? profile.getFirstName() : "",
                         profile.getLastName() != null ? profile.getLastName() : ""
    ).trim();
  }

  public boolean isEmailVerified() {
    return profile != null && profile.isEmailVerified();
  }
}