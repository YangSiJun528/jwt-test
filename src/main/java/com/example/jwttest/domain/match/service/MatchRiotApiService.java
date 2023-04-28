package com.example.jwttest.domain.match.service;

import com.example.jwttest.domain.match.dto.MatchApiDto;
import com.example.jwttest.domain.riot.RiotApiEnvironment;
import org.springframework.stereotype.Service;

@Service
public class MatchRiotApiService {

    private final String BASE_URI = "https://asia.api.riotgames.com/lol/match/v5/matches/";

    public MatchApiDto getMatchIdsByPuuid(String puuid) {
        String uriPath = "by-puuid/{puuid}/" + puuid + "/ids";
        return getMatchDto(uriPath);
    }

    public MatchApiDto getMatchByMatchId(String matchId) {
        String uriPath = matchId;
        return getMatchDto(uriPath);
    }

    private MatchApiDto getMatchDto(String uriPath) {
        String fullUri = BASE_URI + uriPath + "?api_key=" + RiotApiEnvironment.API_KEY;
        return RiotApiEnvironment.REST.getForObject(fullUri, MatchApiDto.class);
    }
}
