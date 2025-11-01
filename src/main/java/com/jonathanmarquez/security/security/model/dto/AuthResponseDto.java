package com.jonathanmarquez.security.security.model.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO para respuestas de autenticación.
 * Contiene información del usuario autenticado.
 */
@Builder
public record AuthResponseDto(
    String username,
    String email,
    String firstName,
    String lastName,
    String fullName,
    String phone,
    List<String> authorities,
    boolean enabled
) {
}