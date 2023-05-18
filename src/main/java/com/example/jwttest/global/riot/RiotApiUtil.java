package com.example.jwttest.global.riot;

import com.example.jwttest.domain.statistics.domain.Statistics;
import com.example.jwttest.domain.summoner.domain.Summoner;
import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.domain.user.dto.UserDto;
import com.example.jwttest.domain.user.enums.Role;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class RiotApiUtil {
    public String API_VERSION() {
        return API_VERSION;
    }

    public String API_KEY() {
        return API_KEY;
    }

    // https://ddragon.leagueoflegends.com/api/versions.json << 여기에서 확인할 수 있음
    @Value("${riot.api-version}")
    private String API_VERSION;

    @Value("${riot.api-key}")
    private String API_KEY;

    public static final RestTemplate REST = new RestTemplate();


    public static String getProfileImgUri(int profileIconId) {
        return "https://ddragon.leagueoflegends.com/cdn/"+"13.9.1"+"/img/profileicon/"+profileIconId+".png";
    }

    public static String getItemImgUri(int itemId) {
        return "https://ddragon.leagueoflegends.com/cdn/"+"13.9.1"+"/img/item/"+itemId+".png";
    }

    public static String getChampionImgUri(String championName) {
        return "https://ddragon.leagueoflegends.com/cdn/"+"13.9.1"+"/img/champion/"+championName+".png";
    }


    public List<Summoner> dummySummoner(User user1, User user2) {
         Summoner s1 = Summoner.builder()
                .id(UUID.randomUUID())
                .summonerApiId("DIiLDPb8BjQewHIbqm1adVUIAObCRiA-wHgAU7mKaGjRNgI")
                .accountId("Pkh25cyxBN_6RQF3qD9WZZ1azpFJj-cqWtsqpYEVe2zMz_g")
                .puuid("JRv9GZ1NllHPUY1DXqQZ66yWwbDNIdi8UDeOtW-4pFxPQMhr17Vc5x1yrhWFehSvyeP2sU3rWiSO2g")
                .name("메추리 알빠노")
                .profileIconId(5389)
                .profileIconIdUri(RiotApiUtil.getProfileImgUri(5389))
                .revisionDate(1683041620434L)
                .summonerLevel(2448)
                .user(user1)
                .build();
         Summoner s2 = Summoner.builder()
                .id(UUID.randomUUID())
                .summonerApiId("vkqcHZK6RT4NX4UfwIe3zWcuZsBRTHhVR4lnKD-JAUWVlOY")
                .accountId("vz3-jvm9gb1SODe3jRshCg1xzkVOXTkt6YbIuy44DlIVSqE")
                .puuid("ad951W1ExSx9ho7R4eYMZvAMB8wEMLvh-Z-azO1zh3gFkGqFg3ESYZySp9ed-O-IbY0N7mMWT813Fg")
                .name("잠은 뒤져서 잔다")
                .profileIconId(5464)
                .profileIconIdUri(RiotApiUtil.getProfileImgUri(5464))
                .revisionDate(1683034046273L)
                .summonerLevel(2422)
                .user(user1)
                .build();
         Summoner s3 = Summoner.builder()
                .id(UUID.randomUUID())
                .summonerApiId("-YWoVxmeUI-MpR8YiM0UtRUcAwbYfG_ZwDjTrf5O1hR5re84DZ5u9uAxBA")
                .accountId("eXGT3kV7bOxG3j0_kfl8WY7l9_sAJp18e-fe9ZzrsIFtcznkqeV9uiuv")
                .puuid("pX1roodpuAb1soUN394FlIpYxPmXJyrsdWUYhQEEpM9SjT5sW-pKWhVXW09_3BusJyxAUQy7Z2n7-A")
                .name("Faker")
                .profileIconId(6)
                .profileIconIdUri(RiotApiUtil.getProfileImgUri(6))
                .revisionDate(1683082584000L)
                .summonerLevel(45)
                .user(user2)
                .build();
        return List.of(s1, s2, s3);
    }

    public List<Statistics> dummyStatistics(List<Summoner> summoners) {
        Statistics st1 = Statistics.init(summoners.get(0));
        Statistics st2 = Statistics.init(summoners.get(1));
        Statistics st3 = Statistics.init(summoners.get(2));
        return List.of(st1, st2, st3);
    }

    public User dummyUser1() {
        return User.builder()
                .id(null)
                .email("Dummy1")
                .name("Dummy1")
                .grade(1)
                .classNum(1)
                .num(11)
                .gender("Dummy1")
                .profileUrl("Dummy1")
                .gAuthRole("Dummy1")
                .role(Role.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public User dummyUser2() {
        return User.builder()
                .id(null)
                .email("Dummy2")
                .name("Dummy2")
                .grade(2)
                .classNum(2)
                .num(22)
                .gender("Dummy2")
                .profileUrl("Dummy2")
                .gAuthRole("Dummy2")
                .role(Role.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private RiotApiUtil() {}

}
