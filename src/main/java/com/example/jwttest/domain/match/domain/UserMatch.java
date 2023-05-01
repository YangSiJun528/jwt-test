package com.example.jwttest.domain.match.domain;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Summoner summoner;

    @OneToOne
    private Match match;

    private String 그외필요한값들;

    public static UserMatch valueOf(Summoner summoner, Match match) {
        return new UserMatch(null, summoner, match, "그외");
    }

}
