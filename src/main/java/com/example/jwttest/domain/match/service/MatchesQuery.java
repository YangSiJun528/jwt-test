package com.example.jwttest.domain.match.service;

import com.example.jwttest.domain.match.domain.Match;
import com.example.jwttest.domain.match.dto.MatchLogResponseDto;
import com.example.jwttest.domain.match.repository.MatchRepository;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.summoner.repository.SummonerRepository;
import com.example.jwttest.global.exception.error.ExpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchesQuery {
    private final SummonerRepository summonerRepository;
    private final MatchRepository matchRepository;

    public Page<MatchLogResponseDto> execute(UUID summonerId, Long startTimestamp, Long endTimestamp, Integer page, Integer size) {
        LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestamp), ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimestamp), ZoneId.systemDefault());
        Summoner summoner = summonerRepository.findById(summonerId)
                .orElseThrow(() -> new ExpectedException("summonerId와 대응되는 Summoner가 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        Page<Match> matches = matchRepository.findAllBySummonerIdAndStartedAtBetween(summoner, startDateTime, endDateTime, PageRequest.of(page, size, Sort.Direction.ASC, "startedAt"));
        log.warn(matches.toString());
        return toDto(matches, summoner);
    }

    public Page<MatchLogResponseDto> execute(UUID summonerId, Integer page, Integer size) {
        Summoner summoner = summonerRepository.findById(summonerId)
                .orElseThrow(() -> new ExpectedException("summonerId와 대응되는 Summoner가 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        Page<Match> matches = matchRepository.findAllBySummonerId(summoner, PageRequest.of(page, size, Sort.Direction.ASC, "startedAt"));
        log.warn(matches.toString());
        return toDto(matches, summoner);
    }

    private Page<MatchLogResponseDto> toDto(Page<Match> matches, Summoner summoner){
        return matches.map(m -> MatchLogResponseDto.from(m, summoner));
    }

}
