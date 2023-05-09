package com.example.jwttest.domain.summoner.dto;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.global.riot.RiotApiUtil;

import java.util.UUID;

public record RegisterSummonerReqDto(
        String accountId
) {
}
