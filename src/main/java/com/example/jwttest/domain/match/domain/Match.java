package com.example.jwttest.domain.match.domain;

import com.example.jwttest.domain.match.converter.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Table(name = "`match`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Match {
    @Id
    @Column(name = "match_id")
    private String id;

    @ElementCollection
    @CollectionTable(name = "MatchSummonerIds", joinColumns =
    @JoinColumn(name = "match_id")
    )
    private List<String> summonerIds;

    @Lob
    @Convert(converter = MapToJsonConverter.class)
    @Column(columnDefinition = "MEDIUMBLOB", nullable = false)
    private Map<String, Object> response;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @OneToMany(mappedBy = "match", fetch = FetchType.LAZY)
    private List<MatchSummoner> summoners;

    @PrePersist
    public void preUpdate() {
        this.startedAt = (this.startedAt == null) ? LocalDateTime.now() : this.startedAt;
    }
}
