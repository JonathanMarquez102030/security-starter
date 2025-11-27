package com.jonathanmarquez.security.email_verification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitud de reenvío de OTP.
 */
public record ResendOtpRequestDto(

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    String email
) {
}