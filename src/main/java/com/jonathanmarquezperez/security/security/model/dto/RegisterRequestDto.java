/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.security.model.dto;


import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

/**
 * DTO para registro de nuevos usuarios.
 */
@Builder
public record RegisterRequestDto(

    @Valid
    @JsonUnwrapped
    UserProfileDto profile,

    @NotBlank(message = "El password es requerido")
    @Size(min = 8, message = "El password debe tener al menos 8 caracteres")
    String password
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
