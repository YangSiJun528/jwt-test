package com.example.jwttest.domain.summoner.dto;

import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.user.dto.UserDto;

public record SummonerResDto(
        String summonerApiId,
        String accountId,
        String puuid,
        String name,
        int profileIconId,
        long revisionDate,
        int summonerLevel,
        boolean isRegistered,
        UserDto userDto // ** nullable ** // TODO 이게 좋은거 같지는 않은데, 괜찮은 해결 방법 생각날때까지는 이렇게 감
) {
    public static SummonerResDto fromRegistered(Summoner summoner) {
        return new SummonerResDto(
                summoner.getSummonerApiId(),
                summoner.getAccountId(),
                summoner.getPuuid(),
                summoner.getName(),
                summoner.getProfileIconId(),
                summoner.getRevisionDate(),
                summoner.getSummonerLevel(),
                true,
                UserDto.from(summoner.getUser())
        );
    }
    public static SummonerResDto fromNonRegistered(SummonerDto summonerDto) {
        return new SummonerResDto(
                summonerDto.id(),
                summonerDto.accountId(),
                summonerDto.puuid(),
                summonerDto.name(),
                summonerDto.profileIconId(),
                summonerDto.revisionDate(),
                summonerDto.summonerLevel(),
                false,
                null
        );
    }
}
