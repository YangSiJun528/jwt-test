package com.example.jwttest.global.batch.dto;

import com.example.jwttest.domain.match.domain.Match;
import com.example.jwttest.domain.statistics.domain.Statistics;

// 나중엔 Match나 DTO Statistics나 DTO가 들어야야 함
// jpql 배치에서 생성자를 사용해서 만드니까
public record MatchStatisticsDto(
        Match match,
        Statistics statistics
) {

}
