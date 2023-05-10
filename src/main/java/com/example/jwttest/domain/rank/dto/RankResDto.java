package com.example.jwttest.domain.rank.dto;

import com.example.jwttest.domain.rank.domain.Rank;
import com.example.jwttest.domain.rank.enums.RankType;
import com.example.jwttest.domain.summoner.dto.SummonerResDto;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public record RankResDto(
        @Enumerated(EnumType.STRING)
        RankType rankType,
        Long rankingNumber,
        String rankValue,
        SummonerResDto summonerResDto

) {
    public static RankResDto from(Rank rank) {
        return new RankResDto(
                rank.getRankType(),
                rank.getRankingNumber(),
                rank.getRankValue(),
                SummonerResDto.fromRegistered(rank.getSummoner())
        );
    }
}
