package com.example.jwttest.global.batch.dto;

public record MatchSummonerBatchDto(
        String matchId,
        byte[] summonerId, // UUID
        String summonerApiId
) {

}
