package com.example.jwttest.domain.match.dto;

import java.util.Map;

public record MatchDto(
        Map<String, Object> metadata,
        Map<String, Object> info
) {
}
