package com.example.jwttest.domain.league.domain;

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
public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    Summoner summoner;

    private String queueType;
    private String tier; // IRON - GOLD
    private String rankNum; // I - III - VI
    private int leaguePoints; // 0 ~ 100
    private int wins;
    private int losses;


    @Column(nullable = false)
    private LocalDateTime modifiedAt;
}
