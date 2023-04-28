package com.example.jwttest.domain.match.dto;

import java.util.ArrayList;

public record Metadata(
        String dataVersion,
        String matchId,
        ArrayList<String> participants
) {}

