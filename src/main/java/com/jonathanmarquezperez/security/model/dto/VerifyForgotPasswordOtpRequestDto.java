/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyForgotPasswordOtpRequestDto(

    @NotBlank @Email String email,

    @NotBlank(message = "El código OTP es requerido")
    @Pattern(regexp = "^[0-9]{6}$", message = "El OTP debe tener 6 dígitos")
    String otpCode
) {
}
