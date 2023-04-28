package com.example.jwttest.domain.match.service;

import com.example.jwttest.domain.match.dto.MatchDto;
import com.example.jwttest.domain.riot.RiotApiEnvironment;
import org.springframework.stereotype.Service;

@Service
public class MatchRiotApiService {

    private final String BASE_URI = "https://asia.api.riotgames.com/lol/match/v5/matches/";

    public MatchDto getMatchIdsByPuuid(String puuid) {
        String uriPath = "by-puuid/{puuid}/" + puuid + "/ids";
        return getMatchDto(uriPath);
    }

    public MatchDto getMatchByMatchId(String matchId) {
        String uriPath = matchId;
        return getMatchDto(uriPath);
    }

    private MatchDto getMatchDto(String uriPath) {
        String fullUri = BASE_URI + uriPath + "?api_key=" + RiotApiEnvironment.API_KEY;
        return RiotApiEnvironment.REST.getForObject(fullUri, MatchDto.class);
    }
}
