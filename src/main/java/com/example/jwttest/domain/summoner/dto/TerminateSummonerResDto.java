package com.example.jwttest.domain.summoner.dto;

import jakarta.validation.constraints.NotBlank;

public record TerminateSummonerResDto(
        @NotBlank
        String accountId
) {
}
