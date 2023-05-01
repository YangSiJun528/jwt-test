package com.example.jwttest;

import com.example.jwttest.global.security.gauth.GauthEnvironment;
import com.example.jwttest.global.security.jwt.JwtEnvironment;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableBatchProcessing // 배티 기능 활성화
@EnableConfigurationProperties({GauthEnvironment.class, JwtEnvironment.class})
public class JwtTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtTestApplication.class, args);
    }

}
