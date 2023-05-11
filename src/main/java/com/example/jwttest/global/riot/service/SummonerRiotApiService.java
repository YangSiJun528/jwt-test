package com.example.jwttest.global.riot.service;

import com.example.jwttest.domain.summoner.dto.SummonerDto;
import com.example.jwttest.global.riot.RiotApiUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SummonerRiotApiService {

    private final String BASE_URI = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/";

    @Scheduled(fixedRate=100) // 웹에서 요청 여러번 보낼수도 있으니까 - 느리다 싶으면 시간 좀 줄이기
    public SummonerDto getSummonerByName(String summonerName) {
        String uriPath = "by-name/" + summonerName;
        return getSummonerDto(uriPath);
    }

    public SummonerDto getSummonerByEncryptedAccountId(String encryptedAccountId) {
        String uriPath = "by-account/" + encryptedAccountId;
        return getSummonerDto(uriPath);
    }

    private SummonerDto getSummonerDto(String uriPath) {
        String fullUri = BASE_URI + uriPath + "?api_key=" + RiotApiUtil.API_KEY;
        return RiotApiUtil.REST.getForObject(fullUri, SummonerDto.class);
    }
}
