package com.example.jwttest.domain.summoner.controller;

import com.example.jwttest.domain.summoner.dto.SummonerDto;
import com.example.jwttest.domain.summoner.service.CreateSummonerService;
import com.example.jwttest.domain.summoner.service.SummonerRiotApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/api/summoner/v1")
@RequiredArgsConstructor
public class SummonerController {

    private final SummonerRiotApiService summonerRiotApiService;

    @GetMapping("/summoner/by-name/{summonerName}")
    public ResponseEntity<SummonerDto> getSummonerByName(@PathVariable String summonerName) {
        return ResponseEntity.status(HttpStatus.OK).body(summonerRiotApiService.getSummonerByName(summonerName));
    }
}
