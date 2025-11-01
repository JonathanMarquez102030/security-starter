package com.jonathanmarquez.security.security.model.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para registro de nuevos usuarios.
 */
public record RegisterRequestDto(
    
    @NotBlank(message = "Username es requerido")
    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    String username,
    
    @NotBlank(message = "Password es requerido")
    @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
    String password,
    
    @NotBlank(message = "Email es requerido")
    @Email(message = "Email inválido")
    String email,
    
    String firstName,
    String lastName,
    String phone
) {
}