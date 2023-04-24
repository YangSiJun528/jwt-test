package com.example.jwttest.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenReqDto(
        @NotBlank String refreshToken
) {
}
