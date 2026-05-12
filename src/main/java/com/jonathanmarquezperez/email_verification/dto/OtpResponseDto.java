/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.email_verification.dto;

import lombok.Builder;

/**
 * DTO para respuestas de operaciones con OTP.
 */
@Builder
public record OtpResponseDto(
    String email,
    String message,
    Integer expirationMinutes
) {
}
