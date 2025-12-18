package com.jonathanmarquez.security.security.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
    @NotBlank @Size(min = 8, max = 200) String newPassword,
    @NotBlank @Size(min = 8, max = 200) String confirmPassword
) {
}