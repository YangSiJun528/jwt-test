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

    // 생각해보니까 MatchSummoner가 있어서 이렇게 번거롭게 안해도 됨

    @Query("SELECT m FROM Match m JOIN m.summoners ms WHERE ms.summoner = :summoner AND m.startedAt BETWEEN :startTimestamp AND :endTimestamp ORDER BY m.startedAt")
    Page<Match> findBySummonerIdAndStartedAtBetween(
            @Param("summoner") Summoner summoner,
            @Param("startTimestamp") LocalDateTime startTimestamp,
            @Param("endTimestamp") LocalDateTime endTimestamp,
            Pageable pageable);

    @Query("SELECT m FROM Match m JOIN m.summoners ms WHERE ms.id = :summonerId ORDER BY m.startedAt")
    Page<Match> findBySummonerId(@Param("summonerId") String summonerId, Pageable pageable);
}
