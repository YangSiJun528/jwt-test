package com.example.jwttest.domain.match.dto;

import java.util.ArrayList;

public record Perks(
        StatPerks statPerks,
        ArrayList<Style> styles
) {
}
