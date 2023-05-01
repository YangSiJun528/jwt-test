package com.example.jwttest.domain.summoner.domain;

import com.example.jwttest.domain.match.domain.MatchUser;
import com.example.jwttest.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Summoner {
    @Id
    @Column(name = "summoner_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String summonerApiId;
    private String accountId;
    private String puuid;
    private String name;
    private int profileIconId;
    private long revisionDate;
    private int summonerLevel;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "summoner")
    private List<MatchUser> matchUsers = new ArrayList<>();
}
