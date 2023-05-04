package com.example.jwttest.global.riot;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RiotApiUtil {
    public static final String API_KEY = "RGAPI-efceb1f0-5602-47b9-9328-878694a5f8b9";
    public static final RestTemplate REST = new RestTemplate();

    // https://ddragon.leagueoflegends.com/api/versions.json << 여기에서 확인할 수 있음
    public static final String IMG_URI_VERSION = "13.9.1";

    public static String getImgUri(int profileIconId) {
        return "https://ddragon.leagueoflegends.com/cdn/"+IMG_URI_VERSION+"/img/profileicon/"+profileIconId+".png";
    }

    private RiotApiUtil() {}

}
