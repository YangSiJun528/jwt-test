package com.example.jwttest.domain.match.repository;

import com.example.jwttest.domain.match.domain.Match;
import com.example.jwttest.domain.summoner.domain.Summoner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    @Query("SELECT m FROM Match m JOIN m.summoners ms WHERE ms.summoner = :summoner AND m.startedAt BETWEEN :startDateTime AND :endDateTime ORDER BY m.startedAt")
    Page<Match> findAllBySummonerIdAndStartedAtBetween(
            @Param("summoner") Summoner summoner,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable);

    @Query("SELECT m FROM Match m JOIN m.summoners ms WHERE ms.summoner = :summoner ORDER BY m.startedAt")
    Page<Match> findAllBySummonerId(@Param("summoner") Summoner summoner, Pageable pageable);
}
