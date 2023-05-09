package com.example.jwttest.domain.summoner.service;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.summoner.dto.SummonerDto;
import com.example.jwttest.domain.summoner.dto.TerminateSummonerResDto;
import com.example.jwttest.domain.summoner.repository.SummonerRepository;
import com.example.jwttest.domain.user.repository.UserRepository;
import com.example.jwttest.global.exception.error.ExpectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class TerminateSummonerService {
    private final UserRepository userRepository;
    private final SummonerRepository summonerRepository;

    // by userId
    public void execute(UUID userId, TerminateSummonerResDto reqDto) {
        if (userRepository.existsById(userId))
            throw new ExpectedException("userID와 대응되는 User가 존재하지 않습니다", HttpStatus.BAD_REQUEST);
        Summoner summoner = summonerRepository.findByAccountId(reqDto.accountId())
                .orElseThrow(() -> new ExpectedException("이미 등록된 Summoner입니다", HttpStatus.BAD_REQUEST));
        summonerRepository.delete(summoner);
    }

}
