package com.jonathanmarquez.security.security.model.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para registro de nuevos usuarios.
 */
public record RegisterRequestDto(

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email es inválido")
    String email,

    @NotBlank(message = "El password es requerido")
    @Size(min = 6, message = "El password debe tener al menos 6 caracteres")
    String password,

    String firstName,
    String lastName,
    String phone,
    LocalDate dateOfBirth
) {
}