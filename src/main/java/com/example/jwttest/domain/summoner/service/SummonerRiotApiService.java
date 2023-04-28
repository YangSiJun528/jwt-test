package com.example.jwttest.domain.summoner.service;

import com.example.jwttest.domain.riot.RiotApiEnvironment;
import com.example.jwttest.domain.summoner.dto.SummonerDto;
import org.springframework.stereotype.Service;

@Service
public class SummonerRiotApiService {

    private final String BASE_URI = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/";

    public SummonerDto getSummonerByName(String summonerName) {
        String uriPath = "by-name/" + summonerName;
        return getSummonerDto(uriPath);
    }

    public SummonerDto getSummonerByEncryptedAccountId(String encryptedAccountId) {
        String uriPath = "by-account/" + encryptedAccountId;
        return getSummonerDto(uriPath);
    }

    private SummonerDto getSummonerDto(String uriPath) {
        String fullUri = BASE_URI + uriPath + "?api_key=" + RiotApiEnvironment.API_KEY;
        return RiotApiEnvironment.REST.getForObject(fullUri, SummonerDto.class);
    }
}
