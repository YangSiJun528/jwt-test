package com.example.jwttest.domain.match.repository;

import com.example.jwttest.domain.match.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
}
