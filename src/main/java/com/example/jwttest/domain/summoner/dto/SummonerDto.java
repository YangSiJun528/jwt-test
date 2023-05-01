package com.example.jwttest.domain.summoner.dto;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.user.domain.User;

import java.util.UUID;

public record SummonerDto(
        String summonerApiId,
        String accountId,
        String puuid,
        String name,
        int profileIconId,
        long revisionDate,
        int summonerLevel
) {
    public Summoner toEntity(User user) {
        return Summoner.builder()
                .user(user)
                .summonerApiId(this.summonerApiId)
                .accountId(this.accountId)
                .puuid(this.puuid)
                .name(this.name)
                .profileIconId(this.profileIconId)
                .revisionDate(this.revisionDate)
                .summonerLevel(this.summonerLevel)
                .build();
    }
}
