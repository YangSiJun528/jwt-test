package com.example.jwttest.domain.summoner.service;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.summoner.dto.SummonerResDto;
import com.example.jwttest.domain.summoner.repository.SummonerRepository;
import com.example.jwttest.global.exception.error.ExpectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SummonersQuery {
    private final SummonerRepository summonerRepository;

    // by userId
    public List<SummonerResDto> execute(UUID userId) {
        List<Summoner> summoners = summonerRepository.findAllByUserId(userId);
        if(summoners.isEmpty()) throw  new ExpectedException("ID와 대응되는 Summoner가 존재하지 않습니다", HttpStatus.BAD_REQUEST);
        List<SummonerResDto> summonerResDtos = new ArrayList<>();
        for(Summoner s : summoners) {
            summonerResDtos.add(SummonerResDto.fromRegistered(s));
        }
        return summonerResDtos;
    }

}
