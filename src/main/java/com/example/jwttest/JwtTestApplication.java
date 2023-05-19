package com.example.jwttest;

import com.example.jwttest.global.security.gauth.GauthEnvironment;
import com.example.jwttest.global.security.jwt.JwtEnvironment;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@OpenAPIDefinition // swagger 설정
@EnableConfigurationProperties({GauthEnvironment.class, JwtEnvironment.class})
public class JwtTestApplication {

    public static void main(String[] args) {
        //SpringApplication.run(JwtTestApplication.class, args);

        /*
         * 배치 시 어플리케이션 정지를 위한 설정
         * https://www.baeldung.com/spring-boot-shutdown << 참고
         */

        SpringApplicationBuilder app = new SpringApplicationBuilder(JwtTestApplication.class);
        app.build().addListeners(new ApplicationPidFileWriter("./bin/application.pid"));
        app.run(args);
    }

}
