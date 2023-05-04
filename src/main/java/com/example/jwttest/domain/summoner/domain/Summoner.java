package com.example.jwttest.domain.summoner.domain;

import com.example.jwttest.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "summoner")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Summoner {
    @Id
    @Column(name = "summoner_id")
    private UUID id;

    @Column(name = "summoner_api_id", unique = true)
    private String summonerApiId;
    private String accountId;
    private String puuid;
    private String name;
    private int profileIconId;
    private String profileIconIdUri;
    private long revisionDate;
    private int summonerLevel;

    @ManyToOne
    private User user;
}
