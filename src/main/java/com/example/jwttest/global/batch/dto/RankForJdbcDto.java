package com.example.jwttest.global.batch.dto;

import com.example.jwttest.domain.rank.enums.RankType;

import java.time.LocalDateTime;
import java.util.UUID;

public record RankForJdbcDto(
        UUID id,
        UUID summonerId,
        Long rankingNumber,
        String rankType, // String임
        String rankValue,
        LocalDateTime createAt
) {
}
