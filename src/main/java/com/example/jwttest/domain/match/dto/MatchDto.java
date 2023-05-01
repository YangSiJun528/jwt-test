package com.example.jwttest.domain.match.dto;

import com.example.jwttest.domain.match.domain.Match;

import java.util.Map;

public record MatchDto(
) {
    public static MatchDto fromDomain(Match match) {
        return new MatchDto();
    }

    public static MatchDto fromApiResponse(Map<String, Object> matchApiResponse) {
        return new MatchDto();
    }
}
