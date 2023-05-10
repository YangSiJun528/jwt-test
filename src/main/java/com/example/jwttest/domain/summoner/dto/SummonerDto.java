package com.example.jwttest.domain.summoner.dto;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.global.riot.RiotApiUtil;

import java.util.UUID;

public record SummonerDto(
        String id,
        String accountId,
        String puuid,
        String name,
        int profileIconId,
        long revisionDate,
        int summonerLevel
) {
    public Summoner toEntity(User user) {
        return Summoner.builder()
                .id(UUID.randomUUID())
                .user(user)
                .summonerApiId(this.id)
                .accountId(this.accountId)
                .puuid(this.puuid)
                .name(this.name)
                .profileIconId(this.profileIconId)
                .profileIconIdUri(RiotApiUtil.getProfileImgUri(this.profileIconId))
                .revisionDate(this.revisionDate)
                .summonerLevel(this.summonerLevel)
                .build();
    }
}
