package com.example.jwttest.domain.statistics.domain;

import com.example.jwttest.domain.summoner.domain.Summoner;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    Summoner summoner;

    Integer maxWinStreak;
    Integer maxLoseStreak;
    Integer curWinStreak;
    Integer curLoseStreak;
    Long winCount;
    Long loseCount;
//    등록 당일날부터 적용되도록 변경했으니까 사용 안함
//    LocalDateTime modifiedDate; // 최소 생성 시 생성 당일 기준 3개월 전 시점, 배치 돌리고 나서 업데이트


    public static Statistics init(Summoner summoner) {
        return new Statistics(
                null,
                summoner,
                0,
                0,
                0,
                0,
                0L,
                0L
        );
    }
}
