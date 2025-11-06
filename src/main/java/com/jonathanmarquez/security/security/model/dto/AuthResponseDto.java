package com.jonathanmarquez.security.security.model.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para respuestas de autenticación.
 * Contiene información del usuario autenticado.
 */
@Builder
public record AuthResponseDto(
    String email,
    String firstName,
    String lastName,
    String fullName,
    String phone,
    LocalDate dateOfBirth,
    List<String> authorities,
    boolean enabled
) {
}