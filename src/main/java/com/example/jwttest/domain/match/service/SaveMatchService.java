package com.example.jwttest.domain.match.service;

import com.example.jwttest.domain.match.dto.MatchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveMatchService {

    private final MatchRiotApiService matchRiotApiService;

    public MatchDto execute(String puuid, Integer start, Integer count, Long startTime, Long endTime) {
        return matchRiotApiService.getMatchIdsByPuuid(puuid, start, count, startTime, endTime);
    }

}
