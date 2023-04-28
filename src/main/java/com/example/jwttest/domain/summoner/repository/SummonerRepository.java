package com.example.jwttest.domain.summoner.repository;

import com.example.jwttest.domain.summoner.domain.Summoner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SummonerRepository extends JpaRepository<Summoner, Long> {
    Optional<Summoner> findByUserId(UUID userId);
}
