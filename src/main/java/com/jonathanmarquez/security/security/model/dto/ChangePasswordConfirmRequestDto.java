package com.jonathanmarquez.security.security.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordConfirmRequestDto(

    @NotBlank @Size(min = 1, max = 200) String currentPassword,

    @NotBlank @Size(min = 8, max = 200) String newPassword,

    @NotBlank @Size(min = 8, max = 200) String confirmPassword,

    @NotBlank(message = "El código OTP es requerido")
    @Pattern(regexp = "^[0-9]{6}$", message = "El OTP debe tener 6 dígitos")
    String otpCode
) {
}