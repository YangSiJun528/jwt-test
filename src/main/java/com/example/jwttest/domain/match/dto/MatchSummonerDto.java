package com.example.jwttest.domain.match.dto;

import com.example.jwttest.domain.match.domain.Match;
import com.example.jwttest.domain.summoner.domain.Summoner;

import java.util.UUID;

// 나중엔 Match나 DTO Statistics나 DTO가 들어야야 함
// jpql 배치에서 생성자를 사용해서 만드니까
public record MatchSummonerDto(
        UUID id,
        String matchId,
        String summonerId,
        String summonerApiId
) {

}
