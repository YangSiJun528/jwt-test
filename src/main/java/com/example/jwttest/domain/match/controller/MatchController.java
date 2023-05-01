package com.example.jwttest.domain.match.controller;

import com.example.jwttest.domain.match.service.MatchRiotApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/match/v1")
@RequiredArgsConstructor
public class MatchController {

    private final MatchRiotApiService matchRiotApiService;

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<Map<String, Object>> getSummonerByName(@PathVariable String matchId) {
        return ResponseEntity.status(HttpStatus.OK).body(matchRiotApiService.getMatchByMatchId(matchId));
    }

    @GetMapping("/matches/by-puuid/{puuid}")
    public ResponseEntity<List<String>> getSummonerByPuuid(@PathVariable String puuid) {
        return ResponseEntity.status(HttpStatus.OK).body(matchRiotApiService.getMatchIdsByPuuid(puuid));
    }
}
