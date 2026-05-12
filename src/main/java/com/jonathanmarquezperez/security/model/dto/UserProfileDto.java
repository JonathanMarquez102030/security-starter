/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Builder
public record UserProfileDto(

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email es inválido")
    String email,

    @NotBlank(message = "El nombre es requerido")
    @Length(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    String firstName,

    @NotBlank(message = "El apellido es requerido")
    @Length(min = 3, max = 100, message = "Los apellidos deben tener entre 3 y 100 caracteres")
    String lastName,

    String phone,

    String profilePictureUrl,

    @NotNull(message = "La fecha de nacimiento es requerida")
    LocalDate dateOfBirth
) {
}
