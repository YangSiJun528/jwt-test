package com.example.jwttest.domain.match.dto;

import java.util.ArrayList;

public record Info(
        long gameCreation,
        int gameDuration,
        long gameEndTimestamp,
        long gameId,
        String gameMode,
        String gameName,
        long gameStartTimestamp,
        String gameType,
        String gameVersion,
        int mapId,
        ArrayList<Participant> participants,
        String platformId,
        int queueId,
        ArrayList<Team> teams,
        String tournamentCode
) {
}
