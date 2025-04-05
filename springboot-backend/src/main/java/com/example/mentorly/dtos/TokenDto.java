package com.example.mentorly.dtos;

public record TokenDto(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}
