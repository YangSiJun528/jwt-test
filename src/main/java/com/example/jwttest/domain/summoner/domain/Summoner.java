package com.example.jwttest.domain.summoner.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Summoner {
    private String id;
    private String accountId;
    private String name;
    private int profileIconId;
    private long revisionDate;
    private int summonerLevel;
}
