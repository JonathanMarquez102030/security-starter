package com.jonathanmarquez.security.security.model.dto;

public record UserProfileDto(
    String email,
    String firstName,
    String lastName,
    String phone,
    String profilePictureUrl,
    String dateOfBirth
) {
}
