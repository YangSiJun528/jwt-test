package com.example.jwttest.global.security.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public final class JwtEnvironment {
    private final String secretKey;
    private final String accessExpiration;
    private final String refreshExpiration;
}
