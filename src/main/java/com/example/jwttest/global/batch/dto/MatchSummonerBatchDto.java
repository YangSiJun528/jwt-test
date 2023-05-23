package com.example.jwttest.global.batch.dto;

import java.util.UUID;

public record MatchSummonerBatchDto(
        String matchId,
        UUID summonerId,
        String summonerApiId
) {

}
