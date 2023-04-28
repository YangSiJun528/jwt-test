package com.example.jwttest.domain.match.dto;

import java.util.Map;

public record MatchApiDto(
        Map<String, Object> metadata,
        Map<String, Object> info
) {
}
