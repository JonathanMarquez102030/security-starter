/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.security.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 200) String newPassword,

    @NotBlank(message = "La confirmación de la contraseña es requerida")
    @Size(min = 8, max = 200) String confirmPassword
) {
}
