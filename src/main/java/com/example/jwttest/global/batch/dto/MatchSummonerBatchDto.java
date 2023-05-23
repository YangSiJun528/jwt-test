package com.example.jwttest.global.batch.dto;

public record MatchSummonerBatchDto(
        byte[] id,
        String matchId,
        String summonerId,
        String summonerApiId
) {

}
