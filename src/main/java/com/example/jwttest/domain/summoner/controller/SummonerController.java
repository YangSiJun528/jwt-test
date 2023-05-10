package com.example.jwttest.domain.summoner.controller;

import com.example.jwttest.domain.summoner.dto.RegisterSummonerReqDto;
import com.example.jwttest.domain.summoner.dto.SummonerResDto;
import com.example.jwttest.domain.summoner.dto.TerminateSummonerResDto;
import com.example.jwttest.domain.summoner.service.RegisterSummonerService;
import com.example.jwttest.domain.summoner.service.SummonerQuery;
import com.example.jwttest.domain.summoner.service.SummonersQuery;
import com.example.jwttest.domain.summoner.service.TerminateSummonerService;
import com.example.jwttest.global.security.jwt.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/summoner/v1")
@RequiredArgsConstructor
public class SummonerController {
    private final SummonerQuery summonerQuery;
    private final SummonersQuery summonersQuery;
    private final RegisterSummonerService registerSummonerService;
    private final TerminateSummonerService terminateSummonerService;

    @GetMapping("summoners/by-user")
    public ResponseEntity<List<SummonerResDto>> getSummonerByUserToken(@AuthenticationPrincipal UserInfo userInfo) {
        return ResponseEntity.ok().body(summonersQuery.execute(userInfo.userId()));
    }

    @GetMapping("summoners/by-user/{userId}")
    public ResponseEntity<List<SummonerResDto>> getSummonerByUserId(@PathVariable String userId) {
        return ResponseEntity.ok().body(summonersQuery.execute(UUID.fromString(userId)));
    }

    // 얘만 응답 형식에 등록 여부 등 좀 다른 DTO
    @GetMapping("summoner/by-name/{summonerName}")
    public ResponseEntity<SummonerResDto> getSummonerBySummonerName(@PathVariable String summonerName) {
        return ResponseEntity.ok().body(summonerQuery.execute(summonerName));
    }

    @GetMapping("summoner/{summonerId}")
    public ResponseEntity<SummonerResDto> getSummonerBySummonerId(@PathVariable UUID summonerId) {
        return ResponseEntity.ok().body(summonerQuery.execute(summonerId));
    }

    @PostMapping("summoner")
    public ResponseEntity<SummonerResDto> registerSummonerByUserToken(
            @AuthenticationPrincipal UserInfo userInfo,
            @RequestBody RegisterSummonerReqDto reqDto
    ) {
        return ResponseEntity.ok().body(registerSummonerService.execute(userInfo.userId(), reqDto));
    }

    @DeleteMapping("summoner")
    public ResponseEntity<Map<String, String>> terminateSummonerByUserToken(
            @AuthenticationPrincipal UserInfo userInfo,
            @RequestBody TerminateSummonerResDto reqDto
    ) {
        terminateSummonerService.execute(userInfo.userId(), reqDto);
        return ResponseEntity.ok().body(Map.of("Message", "Ok"));
    }

//    @PostMapping("summoner/by-user/{userId}") // 이게 맞나?
//    public ResponseEntity<> registerSummonerByUserId(@PathVariable UUID userId) {
//        return ResponseEntity.ok().body();
//    }
//
//    @DeleteMapping("summoner/by-user/{userId}") // 이게 맞나?
//    public ResponseEntity<> registerSummonerByUserId(@PathVariable UUID userId) {
//        return ResponseEntity.ok().body();
//    }
}
