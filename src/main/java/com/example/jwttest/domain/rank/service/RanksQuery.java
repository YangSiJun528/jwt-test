package com.example.jwttest.domain.rank.service;

import com.example.jwttest.domain.rank.domain.Rank;
import com.example.jwttest.domain.rank.dto.RankResDto;
import com.example.jwttest.domain.rank.enums.RankType;
import com.example.jwttest.domain.rank.repository.RankRepository;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.summoner.repository.SummonerRepository;
import com.example.jwttest.global.exception.error.ExpectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RanksQuery {
    private final SummonerRepository summonerRepository;
    private final RankRepository rankRepository;
    public List<RankResDto> execute(UUID summonerId) {
        Summoner summoner = summonerRepository.findById(summonerId)
                .orElseThrow(() -> new ExpectedException("summonerId와 대응되는 User가 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        List<Rank> ranks = rankRepository.findAllBySummonerOrderByRankingNumberAsc(summoner);
        return toDto(ranks);
    }

    public List<RankResDto> execute(RankType rankType) {
        List<Rank> ranks = rankRepository.findAllByRankTypeOrderByRankTypeAscRankingNumberAsc(rankType);
        return toDto(ranks);
    }

    private List<RankResDto> toDto(List<Rank> ranks){
        return ranks.stream().map(RankResDto::from).toList();
    }
}
