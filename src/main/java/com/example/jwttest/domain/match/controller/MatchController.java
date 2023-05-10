package com.example.jwttest.domain.match.controller;

import com.example.jwttest.domain.match.dto.MatchLogResponseDto;
import com.example.jwttest.domain.match.service.MatchesQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/api/match/v1")
@RequiredArgsConstructor
public class MatchController {

    private final MatchesQuery matchesQuery;

    @GetMapping("/matches")
    public ResponseEntity<Page<MatchLogResponseDto>> summonerByUserByToken(
            @RequestParam(value = "summonerId", required = true) UUID summonerId,
            @RequestParam(value = "startTimestamp", required = false) Long startTimestamp,
            @RequestParam(value = "endTimestamp", required = false) Long endTimestamp,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        log.warn("summonerId={}, startTimestamp={}, endTimestamp={}, page={}, size={}",summonerId, startTimestamp, endTimestamp, page, size);
        // 원래 startTimestamp endTimestamp 중 하나만 있을 때도 동작할 수 있는데, 귀찮으니까
        if (startTimestamp == null || endTimestamp == null) {
            return ResponseEntity.ok().body(matchesQuery.execute(summonerId, page, size));
        }
        return ResponseEntity.ok().body(matchesQuery.execute(summonerId, startTimestamp, endTimestamp, page, size));
    }
}
