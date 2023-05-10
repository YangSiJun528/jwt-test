package com.example.jwttest.domain.rank.repository;

import com.example.jwttest.domain.rank.domain.Rank;
import com.example.jwttest.domain.rank.enums.RankType;
import com.example.jwttest.domain.summoner.domain.Summoner;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RankRepository extends JpaRepository<Rank, UUID> {
    @EntityGraph(attributePaths = {"summoner", "summoner.user"})
    List<Rank> findAllBySummonerOrderByRankingNumberAsc(Summoner summoner);

    @EntityGraph(attributePaths = {"summoner", "summoner.user"})
    List<Rank> findAllByRankTypeOrderByRankTypeAscRankingNumberAsc(RankType rankType);
}
