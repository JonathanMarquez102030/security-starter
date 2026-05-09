/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.email_verification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para solicitud de verificación de OTP.
 */
public record VerifyOtpRequestDto(

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    String email,

    @NotBlank(message = "El código OTP es requerido")
    @Pattern(regexp = "^\\d{6}$", message = "El código OTP debe ser de 6 dígitos numéricos")
    String otpCode
) {
}
