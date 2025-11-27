package com.jonathanmarquez.security.security.model.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.Valid;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para respuestas de autenticación.
 * Contiene información del usuario autenticado.
 */
@Builder
public record AuthResponseDto(

    @Valid
    @JsonUnwrapped
    UserProfileDto profile,

    String fullName,

    List<String> authorities,

    boolean enabled
) {

  public String email() {
    return this.profile.email();
  }

  public String firstName() {
    return this.profile.firstName();
  }

  public String lastName() {
    return this.profile.lastName();
  }

  public String phone() {
    return this.profile.phone();
  }

  public String profilePictureUrl() {
    return this.profile.profilePictureUrl();
  }

  public LocalDate dateOfBirth() {
    return this.profile.dateOfBirth();
  }
}