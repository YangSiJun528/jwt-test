package com.example.jwttest.domain.statistics.domain;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @OneToOne
    Summoner summoner;

    Integer maxWinStreak;
    Integer maxLoseStreak;
    Integer curWinStreak;
    Integer curLoseStreak;
    Long winCount;
    Long loseCount;
    LocalDateTime modifiedDate; // 최소 생성 시 생성 당일 기준 3개월 전 시점, 배치 돌리고 나서 업데이트

}
