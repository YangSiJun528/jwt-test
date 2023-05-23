package com.example.jwttest.domain.rank.domain;

import com.example.jwttest.domain.rank.enums.RankType;
import com.example.jwttest.domain.summoner.domain.Summoner;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "`rank`", uniqueConstraints = @UniqueConstraint(columnNames = {"rankType", "SUMMONER_SUMMONER_ID"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Rank {
    @Id
    @Column(name = "rank_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    Summoner summoner;

    Long rankingNumber;

    @Enumerated(EnumType.STRING)
    RankType rankType;

    String rankValue;

    @Column(nullable = false)
    private LocalDateTime createAt;
}
