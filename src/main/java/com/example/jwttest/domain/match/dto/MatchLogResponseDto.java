package com.example.jwttest.domain.match.dto;

import com.example.jwttest.domain.match.domain.Match;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.global.riot.RiotApiUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public record MatchLogResponseDto (

        // 매치 ID
        // 매치 시간대
        // KDA dka
        // 총 cs, 분당 cs
        // 챔피엔 및 레벨
        // 승패 여부
        // 아이템, 스펠, 룬 정보 추출
        // rune 은 id
        // 나머지는 uri
        String matchId, String gameMode, Long gameCreation, int gameDuration, Long gameEndTimestamp, String gameType, int mapId,
        boolean win, int champLevel, int championId, String championName, String championProfileUri,
        // double kda,
        int deaths, int kills, int assists,
        int summoner1Id, int summoner2Id,
        int primaryStyle, int subStyle,
        int item0, int item1, int item2, int item3, int item4, int item5, int item6,
        String item0Uri, String item1Uri, String item2Uri, String item3Uri, String item4Uri, String item5Uri, String item6Uri
) {
    public static MatchLogResponseDto from(Match match, Summoner summoner) {
        Map<String, Object> info = (Map<String, Object>) match.getResponse().get("info");
        log.warn("info={}",info);
        List<Map<String, Object>> participants = (List<Map<String, Object>>) info.get("participants");
        Map<String, Object> participant = participants.stream().filter((p) -> p.get("summonerId").equals(summoner.getSummonerApiId())).findAny()
                .orElseThrow(() -> new RuntimeException("입력한 Summoner를 Match에서 찾을 수 없습니다"));
        Map<String, Object> challenges = (Map<String, Object>) participant.get("challenges");
        Map<String, Object> perks = (Map<String, Object>) participant.get("perks");
        List<Map<String, Object>> styles = (List<Map<String, Object>>) perks.get("styles");

        String matchId = match.getId();
        String gameMode = (String) info.get("gameMode");
        Long gameCreation = (Long) info.get("gameCreation");
        int gameDuration = (int) info.get("gameDuration");
        Long gameEndTimestamp = (Long) info.get("gameEndTimestamp");
        String gameType = (String) info.get("gameType");
        int mapId = (int) info.get("mapId");
        boolean win = (boolean) participant.get("win");

        int champLevel = (int) participant.get("champLevel");
        int championId = (int) participant.get("championId");
        String championName = (String) participant.get("championName");

//        double kda = (double) challenges.get("kda");
        int deaths = (int) participant.get("deaths");
        int kills = (int) participant.get("kills");
        int assists = (int) participant.get("assists");

        int summoner1Id = (int) participant.get("summoner1Id"); // int로 주는데, 실제로 이게 스펠인지 모름;;; 심지어 라이엇이 주는 스펠 정보 json에도 id가 없음;;
        int summoner2Id = (int) participant.get("summoner2Id");


        int primaryStyle = -1;
        int subStyle = -1;
        for (Map<String, Object> style : styles) {
            if (style.get("description").equals("primaryStyle")) {
                primaryStyle = (int) style.get("style");
            } else if (style.get("description").equals("subStyle")) {
                subStyle = (int) style.get("style");
            }
        }

        int item0 = (int) participant.get("item0");
        int item1 = (int) participant.get("item1");
        int item2 = (int) participant.get("item2");
        int item3 = (int) participant.get("item3");
        int item4 = (int) participant.get("item4");
        int item5 = (int) participant.get("item5");
        int item6 = (int) participant.get("item6");

        String championProfileUri = RiotApiUtil.getChampionImgUri(championId);

        String item0Uri = RiotApiUtil.getItemImgUri(item0);
        String item1Uri = RiotApiUtil.getItemImgUri(item1);
        String item2Uri = RiotApiUtil.getItemImgUri(item2);
        String item3Uri = RiotApiUtil.getItemImgUri(item3);
        String item4Uri = RiotApiUtil.getItemImgUri(item4);
        String item5Uri = RiotApiUtil.getItemImgUri(item5);
        String item6Uri = RiotApiUtil.getItemImgUri(item6);

        return new MatchLogResponseDto(
                matchId, gameMode, gameCreation, gameDuration, gameEndTimestamp, gameType, mapId, win,
                champLevel, championId, championName, championProfileUri,
                //kda,
                deaths, kills, assists, summoner1Id, summoner2Id,
                primaryStyle, subStyle,
                item0, item1, item2, item3, item4, item5, item6,
                item0Uri, item1Uri, item2Uri, item3Uri, item4Uri, item5Uri, item6Uri
        );


//                for (Map<String, Object> style : styles) {
//            if (style.get("style").equals("primaryStyle")) {
//                int primaryStyle = (int) participant.get("item0");
//                int primaryStyleSelection1 = (int) participant.get("item0");
//                int primaryStyleSelection2 = (int) participant.get("item0");
//                int primaryStyleSelection3 = (int) participant.get("item0");
//            } else if (style.get("style").equals("subStyle")) {
//                List<Map<String, Object>> selections = (List<Map<String, Object>>) style.get("item0");
//                int subStyle = (int) style.get("subStyle");
//                for (Map<String, Object> selection : selections) {
//                    int subStyleSelection1 = (int) selections.get("selections");
//                    int subStyleSelection2 = (int) selections.get("item0");
//                }
//            }
//        }

        // List<Map<String, Object>> runeInfo = styles;

    }
}
