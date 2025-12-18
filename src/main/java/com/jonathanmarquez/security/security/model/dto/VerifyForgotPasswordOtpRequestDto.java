package com.jonathanmarquez.security.security.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyForgotPasswordOtpRequestDto(
    @NotBlank @Email String email,
    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "El OTP debe tener 6 dígitos")
    String otpCode
) {
}