package com.example.jwttest.domain.summoner.service;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.summoner.dto.SummonerDto;
import com.example.jwttest.domain.summoner.dto.SummonerResDto;
import com.example.jwttest.domain.summoner.repository.SummonerRepository;
import com.example.jwttest.global.exception.error.ExpectedException;
import com.example.jwttest.global.riot.service.SummonerRiotApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SummonerQuery {
    private final SummonerRepository summonerRepository;
    private final SummonerRiotApiService summonerRiotApiService;

    // by summonerId
    public SummonerResDto execute(UUID summonerId) {
        Summoner summoner = summonerRepository.findById(summonerId)
                .orElseThrow(() -> new ExpectedException("summonerId와 대응되는 User가 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        return SummonerResDto.fromRegistered(summoner);
    }

    // by summonerName
    public SummonerResDto execute(String summonerName) {
        return summonerRepository.findByName(summonerName)
                .map(SummonerResDto::fromRegistered)
                .orElseGet(() -> SummonerResDto.fromNonRegistered(getSummonerDto(summonerName)));
    }

    private SummonerDto getSummonerDto(String summonerName) {
        try {
            return summonerRiotApiService.getSummonerByName(summonerName);
        } catch (HttpClientErrorException e) {
            if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ExpectedException("summonerName을 가지는 Summoner가 존재하지 않습니다", HttpStatus.BAD_REQUEST);
            }
            throw e;
        }
    }

}
