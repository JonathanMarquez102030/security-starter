package com.jonathanmarquez.security.email_verification.dto;

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