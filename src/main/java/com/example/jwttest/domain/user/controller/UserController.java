package com.example.jwttest.domain.user.controller;

import com.example.jwttest.global.security.gauth.GauthEnvironment;
import gauth.GAuth;
import gauth.GAuthToken;
import gauth.GAuthUserInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("auth")
public class UserController {

    private final GauthEnvironment env;

    private final GAuth gAuth;

    private String CLIENT_ID;
    private String CLIENT_SECRET;
    private String REDIRECT_URI;

    public UserController(GauthEnvironment env, GAuth gAuth) {
        this.env = env;
        this.CLIENT_ID = env.getClientId();
        this.CLIENT_SECRET = env.getClientSecret();
        this.REDIRECT_URI = env.getRedirectUri();
        this.gAuth = gAuth;
    }

    @GetMapping("gauth/code")
    public ResponseEntity<String> gauthCode(@RequestParam(value="code") String code) throws IOException {

        GAuthToken generateToken = gAuth.generateToken(code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
        GAuthUserInfo gAuthUserInfo = gAuth.getUserInfo(generateToken.getAccessToken());

        return ResponseEntity.ok().body(gAuthUserInfo.toString());
    }

    @GetMapping("login") // http://localhost:8080/auth/login
    public void login(HttpServletResponse httpServletResponse) {
        log.info("CLIENT_ID : {}", CLIENT_ID);
        log.info("REDIRECT_URL : {}", REDIRECT_URI);
        try {
            httpServletResponse.sendRedirect(String.format("https://gauth.co.kr/login?client_id=%s&redirect_uri=%s", CLIENT_ID, REDIRECT_URI));
        } catch (IOException e) {
            throw new RuntimeException("IOException has occurred",e);
        }
    }
}
