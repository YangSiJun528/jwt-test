package com.example.jwttest.domain.summoner.dto;

public record SummonerDto(
        String id,
        String accountId,
        String name,
        int profileIconId,
        long revisionDate,
        int summonerLevel
) {
}
