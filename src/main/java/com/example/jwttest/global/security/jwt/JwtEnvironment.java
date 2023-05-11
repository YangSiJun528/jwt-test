package com.example.jwttest.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtEnvironment(
        String secretKey,
        Long accessExpiration,
        Long refreshExpiration,
        String issuer
) {
}
