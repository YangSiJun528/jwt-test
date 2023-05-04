package com.example.jwttest.domain.match.domain;

import com.example.jwttest.domain.match.converter.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Match {
    @Id
    @Column(name = "match_id")
    String id;

    @ElementCollection
    @CollectionTable(name = "MatchSummonerIds", joinColumns =
    @JoinColumn(name = "match_id")
    )
    private List<String> summonerIds;

    @Lob
    @Convert(converter = MapToJsonConverter.class)
    @Column(nullable = false, unique = true)
    Map<String, Object> json;
}
