package com.example.jwttest.domain.statistics.repository;

import com.example.jwttest.domain.statistics.domain.Statistics;
import com.example.jwttest.domain.summoner.domain.Summoner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StatisticsRepository extends JpaRepository<Statistics, UUID> {
    Optional<Statistics> findBySummoner(Summoner summoner);
}
