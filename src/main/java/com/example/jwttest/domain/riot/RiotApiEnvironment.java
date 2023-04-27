package com.example.jwttest.domain.riot;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RiotApiEnvironment {
    public static final String API_KEY = "RGAPI-4b754cf3-3199-4c83-a0a3-01e7720f9662";
    public static final RestTemplate REST = new RestTemplate();

}
