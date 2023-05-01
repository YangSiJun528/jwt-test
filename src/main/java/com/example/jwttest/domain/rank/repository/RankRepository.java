package com.example.jwttest.domain.rank.repository;

import com.example.jwttest.domain.rank.domain.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RankRepository extends JpaRepository<Rank, UUID> {
}
