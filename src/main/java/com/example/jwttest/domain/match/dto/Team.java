package com.example.jwttest.domain.match.dto;

import java.util.ArrayList;

public record Team(
        ArrayList<Ban> bans,
        Objectives objectives,
        int teamId,
        boolean win
) {}





