package com.example.jwttest.global.riot;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RiotApiEnvironment {
    public static final String API_KEY = "RGAPI-efceb1f0-5602-47b9-9328-878694a5f8b9";
    public static final RestTemplate REST = new RestTemplate();

}
