package com.example.jwttest.domain.summoner.service;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.summoner.dto.RegisterSummonerReqDto;
import com.example.jwttest.domain.summoner.dto.SummonerDto;
import com.example.jwttest.domain.summoner.dto.SummonerResDto;
import com.example.jwttest.domain.summoner.repository.SummonerRepository;
import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.domain.user.repository.UserRepository;
import com.example.jwttest.global.exception.error.ExpectedException;
import com.example.jwttest.global.riot.service.SummonerRiotApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class RegisterSummonerService {
    private final UserRepository userRepository;
    private final SummonerRepository summonerRepository;
    private final SummonerRiotApiService summonerRiotApiService;

    // by userId
    public SummonerResDto execute(UUID userId, RegisterSummonerReqDto reqDto) {
        if(summonerRepository.existsByAccountId(reqDto.accountId())) {
            throw new ExpectedException("이미 등록된 Summoner입니다", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException("userID와 대응되는 User가 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        SummonerDto summonerDto = getSummonerDto(reqDto.accountId());
        Summoner savedSummoner = summonerRepository.save(summonerDto.toEntity(user));
        return SummonerResDto.fromRegistered(savedSummoner);
    }

    private SummonerDto getSummonerDto(String accountId) {
        try {
            SummonerDto summonerDto = summonerRiotApiService.getSummonerByEncryptedAccountId(accountId);
            return summonerDto;
        } catch (HttpClientErrorException e) {
            if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ExpectedException("summonerName을 가지는 Summoner가 존재하지 않습니다", HttpStatus.BAD_REQUEST);
            }
        }
        throw new IllegalStateException("getSummonerDto에 의도하지 않은 결과가 발생했습니다.");
    }

}
