package com.example.jwttest.domain.user.dto;

public record TokenResDto(
        String accessToken,
        String refreshToken
) {
}
