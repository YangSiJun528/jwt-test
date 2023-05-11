package com.example.jwttest.global.security.gauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gauth")
public record GauthEnvironment(
        String clientId,
        String clientSecret,
        String redirectUri
) {
}
