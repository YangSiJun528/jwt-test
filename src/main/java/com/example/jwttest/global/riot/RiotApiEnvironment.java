package com.example.jwttest.global.riot;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RiotApiEnvironment {
    public static final String API_KEY = "RGAPI-2891424e-d074-436d-abd0-54731226a959";
    public static final RestTemplate REST = new RestTemplate();

}
