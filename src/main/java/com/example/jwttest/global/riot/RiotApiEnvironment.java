package com.example.jwttest.global.riot;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RiotApiEnvironment {
    public static final String API_KEY = "RGAPI-6f5de59a-97bc-4f1d-a69b-172ada0a4760";
    public static final RestTemplate REST = new RestTemplate();

}
