package com.example.jwttest.domain.statistics.repository;

import com.example.jwttest.domain.statistics.domain.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StatisticsRepository extends JpaRepository<Statistics, UUID> {
}
