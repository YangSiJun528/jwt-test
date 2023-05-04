package com.example.jwttest.global.riot;

import com.example.jwttest.domain.summoner.domain.Summoner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Component
public class RiotApiUtil {
    public static final String API_KEY = "RGAPI-efceb1f0-5602-47b9-9328-878694a5f8b9";
    public static final RestTemplate REST = new RestTemplate();

    // https://ddragon.leagueoflegends.com/api/versions.json << 여기에서 확인할 수 있음
    public static final String IMG_URI_VERSION = "13.9.1";

    public static String getImgUri(int profileIconId) {
        return "https://ddragon.leagueoflegends.com/cdn/"+IMG_URI_VERSION+"/img/profileicon/"+profileIconId+".png";
    }

    public static List<Summoner> dummySummoner() {
        Summoner s1 = Summoner.builder()
                .id(UUID.randomUUID())
                .summonerApiId("DIiLDPb8BjQewHIbqm1adVUIAObCRiA-wHgAU7mKaGjRNgI")
                .accountId("Pkh25cyxBN_6RQF3qD9WZZ1azpFJj-cqWtsqpYEVe2zMz_g")
                .puuid("JRv9GZ1NllHPUY1DXqQZ66yWwbDNIdi8UDeOtW-4pFxPQMhr17Vc5x1yrhWFehSvyeP2sU3rWiSO2g")
                .name("메추리 알빠노")
                .profileIconId(5389)
                .profileIconIdUri(RiotApiUtil.getImgUri(5389))
                .revisionDate(1683041620434L)
                .summonerLevel(2448)
                .build();
        Summoner s2 = Summoner.builder()
                .id(UUID.randomUUID())
                .summonerApiId("vkqcHZK6RT4NX4UfwIe3zWcuZsBRTHhVR4lnKD-JAUWVlOY")
                .accountId("vz3-jvm9gb1SODe3jRshCg1xzkVOXTkt6YbIuy44DlIVSqE")
                .puuid("ad951W1ExSx9ho7R4eYMZvAMB8wEMLvh-Z-azO1zh3gFkGqFg3ESYZySp9ed-O-IbY0N7mMWT813Fg")
                .name("잠은 뒤져서 잔다")
                .profileIconId(5464)
                .profileIconIdUri(RiotApiUtil.getImgUri(5464))
                .revisionDate(1683034046273L)
                .summonerLevel(2422)
                .build();
        Summoner s3 = Summoner.builder()
                .id(UUID.randomUUID())
                .summonerApiId("-YWoVxmeUI-MpR8YiM0UtRUcAwbYfG_ZwDjTrf5O1hR5re84DZ5u9uAxBA")
                .accountId("eXGT3kV7bOxG3j0_kfl8WY7l9_sAJp18e-fe9ZzrsIFtcznkqeV9uiuv")
                .puuid("pX1roodpuAb1soUN394FlIpYxPmXJyrsdWUYhQEEpM9SjT5sW-pKWhVXW09_3BusJyxAUQy7Z2n7-A")
                .name("Faker")
                .profileIconId(6)
                .profileIconIdUri(RiotApiUtil.getImgUri(6))
                .revisionDate(1683082584000L)
                .summonerLevel(45)
                .build();
        return List.of(s1, s2, s3);
    }

    private RiotApiUtil() {}

}
