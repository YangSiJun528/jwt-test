package com.example.jwttest.global.riot.service;

import com.example.jwttest.global.riot.RiotApiUtil;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchRiotApiService {

    private final RiotApiUtil riotApiUtil;

    private final String BASE_URI = "https://asia.api.riotgames.com/lol/match/v5/matches/";

    public Map<String, Object> getMatchByMatchId(String matchId) {
        String uriPath = matchId;
        return getApiMatch(uriPath);
    }

    private Map<String, Object> getApiMatch(String uriPath) {
        String fullUri = BASE_URI + uriPath + "?api_key=" + riotApiUtil.API_KEY();
        return RiotApiUtil.REST.getForObject(fullUri, Map.class);
    }

    // TODO 나중에 리팩토링
    public List<String> getMatchIdsByPuuid(String puuid, Integer start, Integer count, LocalDateTime startTime, LocalDateTime endTime) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("by-puuid/" + puuid + "/ids");

        Assert.notNull(start);
        Assert.notNull(count);
        if (startTime != null) {
            uriBuilder.queryParam("startTime", startTime.toEpochSecond(ZoneOffset.UTC));
            log.warn(String.valueOf(startTime.toEpochSecond(ZoneOffset.UTC)));
        }
        if (endTime != null) {
            uriBuilder.queryParam("endTime", endTime.toEpochSecond(ZoneOffset.UTC));
            log.warn(String.valueOf(endTime.toEpochSecond(ZoneOffset.UTC)));
        }
        uriBuilder.queryParam("start", start);
        uriBuilder.queryParam("count", count);

        String uriPath = uriBuilder.build().toString();

        String fullUri = BASE_URI + uriPath + "&api_key=" + riotApiUtil.API_KEY();
        return RiotApiUtil.REST.getForObject(fullUri, List.class);
    }

    public List<String> getMatchIdsByPuuid(String puuid) {
        return getMatchIdsByPuuid(puuid, 0, 20, null, null);
    }
}
