package com.example.jwttest.domain.match.domain;

import com.example.jwttest.domain.match.converter.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
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
    Map<String, Object> response;

    @Column(nullable = false)
    private LocalDateTime createAt;

    @PrePersist
    public void preUpdate() {
        this.createAt = (this.createAt == null) ? LocalDateTime.now() : this.createAt;
    }
}
