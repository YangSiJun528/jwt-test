package com.example.jwttest.domain.match.service;

import com.example.jwttest.global.riot.RiotApiEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MatchRiotApiService {

    private final String BASE_URI = "https://asia.api.riotgames.com/lol/match/v5/matches/";

    public Map<String, Object> getMatchByMatchId(String matchId) {
        String uriPath = matchId;
        return getApiMatch(uriPath);
    }

    private Map<String, Object> getApiMatch(String uriPath) {
        String fullUri = BASE_URI + uriPath + "?api_key=" + RiotApiEnvironment.API_KEY;
        log.info(fullUri);
        return RiotApiEnvironment.REST.getForObject(fullUri, Map.class);
    }

    // TODO 나중에 리팩토링
    public List<String> getMatchIdsByPuuid(String puuid, Integer start, Integer count, Long startTime, Long endTime) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("by-puuid/" + puuid + "/ids");

        if (start == null || count == null) {
            throw new IllegalArgumentException("null일 수 없음");
        }
        if (startTime != null) {
            uriBuilder.queryParam("startTime", startTime);
        }
        if (endTime != null) {
            uriBuilder.queryParam("endTime", endTime);
        }
        uriBuilder.queryParam("start", start);
        uriBuilder.queryParam("count", count);

        String uriPath = uriBuilder.build().toString();

        String fullUri = BASE_URI + uriPath + "&api_key=" + RiotApiEnvironment.API_KEY;
        return RiotApiEnvironment.REST.getForObject(fullUri, List.class);
    }

    public List<String> getMatchIdsByPuuid(String puuid) {
        return getMatchIdsByPuuid(puuid, 0, 20, null, null);
    }
}
