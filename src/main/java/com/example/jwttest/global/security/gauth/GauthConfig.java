package com.example.jwttest.global.security.gauth;

import gauth.GAuth;
import gauth.impl.GAuthImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GauthConfig {

    @Bean
    public GAuth gauth() {
        return new GAuthImpl();
    }

}
