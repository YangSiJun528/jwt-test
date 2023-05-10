package com.example.jwttest.domain.rank.controller;

import com.example.jwttest.domain.rank.dto.RankResDto;
import com.example.jwttest.domain.rank.enums.RankType;
import com.example.jwttest.domain.rank.service.RanksQuery;
import com.example.jwttest.global.exception.error.ExpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/rank/v1")
@RequiredArgsConstructor
public class RankController {

    private final RanksQuery ranksQuery;

//    @GetMapping("ranks/by-user")
//    public ResponseEntity<> ranksByUserToken(@AuthenticationPrincipal UserInfo userInfo) {
//        return ResponseEntity.ok().body();
//    }

    @GetMapping("/ranks/by-summoner/{summonerId}")
    public ResponseEntity<List<RankResDto>> ranksByUserId(@PathVariable UUID summonerId) {
        return ResponseEntity.ok().body(ranksQuery.execute(summonerId));
    }

    @GetMapping("/ranks/by-category/{category}")
    public ResponseEntity<List<RankResDto>> summonerByUserByToken(@PathVariable String category) {
        try {
            RankType rankType = RankType.valueOf(category);
            return ResponseEntity.ok().body(ranksQuery.execute(rankType));
        } catch (IllegalArgumentException e) {
            throw new ExpectedException("category와 대응되는 RankType가 존재하지 않습니다", HttpStatus.BAD_REQUEST);
        }
    }
}
