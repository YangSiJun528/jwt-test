package com.example.jwttest.global.security.gauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "gauth")
public final class GauthEnvironment {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
}
