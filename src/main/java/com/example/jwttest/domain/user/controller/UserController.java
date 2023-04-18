package com.example.jwttest.domain.user.controller;

import com.example.jwttest.global.security.GauthEnvironment;
import gauth.GAuth;
import gauth.GAuthCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("auth")
public class UserController {

    private final GauthEnvironment env;

    private final GAuth gAuth;

    private String CLIENT_ID;
    private String CLIENT_SECRET;
    private String REDIRECT_URL;

    public UserController(GauthEnvironment env, GAuth gAuth) {
        this.env = env;
        this.CLIENT_ID = env.getClientId();
        this.CLIENT_SECRET = env.getClientSecret();
        this.REDIRECT_URL = env.getRedirectUrl();
        this.gAuth = gAuth;
    }

    @GetMapping("gauth/code")
    public ResponseEntity<String> gauthCode(@RequestParam(value="code") String code) {
        return ResponseEntity.ok().body(code);
    }

    @GetMapping("login11")
    public ResponseEntity<String> login11() {
        String str;
        try {
           GAuthCode code = gAuth.generateCode("s21011", "aaaaaaaaaa");
            str = code.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().body(str);
    }

    @GetMapping("/authentication")
    public ResponseEntity<String> authentication() {
        String url = "https://server.gauth.co.kr/oauth/token";
        RestTemplate rt = new RestTemplate();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", "hong");
        body.add("clientId", CLIENT_ID);
        body.add("clientSecret", CLIENT_SECRET);
        body.add("redirectUri", REDIRECT_URL);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MultiValueMap<String, String>> requestMessage = new HttpEntity<>(body, httpHeaders);

        HttpEntity<String> response = rt.postForEntity(
                url, // 요청할 서버 주소
                requestMessage, // 요청할 때 보낼 데이터 - Header+Body
                String.class // 반환 데이터 타입
        );
        return ResponseEntity.ok().body(response.toString());
    }

    @GetMapping("login") // // http://localhost:8080/auth/login
    public void login(HttpServletResponse httpServletResponse) {
        log.info("CLIENT_ID : {}", CLIENT_ID);
        log.info("REDIRECT_URL : {}", REDIRECT_URL);
        try {
            httpServletResponse.sendRedirect(String.format("https://gauth.co.kr/login?client_id=%s&redirect_url=%s", CLIENT_ID, REDIRECT_URL));
        } catch (IOException e) {
            throw new RuntimeException("IOException has occurred",e);
        }
    }
}
