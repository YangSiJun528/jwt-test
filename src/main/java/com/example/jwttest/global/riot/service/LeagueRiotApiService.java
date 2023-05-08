package com.example.jwttest.global.riot.service;

import com.example.jwttest.domain.summoner.dto.SummonerDto;
import com.example.jwttest.global.riot.RiotApiUtil;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class LeagueRiotApiService {

    private final String BASE_URI = "https://kr.api.riotgames.com/lol/league/v4/";

    public Set<Map<String, Object>> getLeagueBySummonerId(String summonerId) {
        String uriPath = "entries/by-summoner/" + summonerId;
        return getLeagueDto(uriPath);
    }

    private Set<Map<String, Object>> getLeagueDto(String uriPath) {
        String fullUri = BASE_URI + uriPath + "?api_key=" + RiotApiUtil.API_KEY;
        return RiotApiUtil.REST.getForObject(fullUri, Set.class);
    }
}
