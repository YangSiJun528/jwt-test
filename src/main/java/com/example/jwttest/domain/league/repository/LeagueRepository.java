package com.example.jwttest.domain.league.repository;

import com.example.jwttest.domain.league.domain.League;
import com.example.jwttest.domain.summoner.domain.Summoner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeagueRepository extends JpaRepository<League, UUID> {
    Optional<League> findBySummoner(Summoner summoner);
}
