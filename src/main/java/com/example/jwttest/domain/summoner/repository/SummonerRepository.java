package com.example.jwttest.domain.summoner.repository;

import com.example.jwttest.domain.summoner.domain.Summoner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SummonerRepository extends JpaRepository<Summoner, UUID> {
    List<Summoner> findAllByUserId(UUID userId);
    Optional<Summoner> findByName(String summonerName);
    boolean existsByAccountId(String accountId);
    Optional<Summoner> findByAccountId(String accountId);
}
