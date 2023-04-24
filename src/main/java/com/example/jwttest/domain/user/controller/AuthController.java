package com.example.jwttest.domain.user.controller;

import com.example.jwttest.domain.user.dto.RefreshTokenReqDto;
import com.example.jwttest.domain.user.dto.TokenResDto;
import com.example.jwttest.domain.user.service.RefreshTokenService;
import com.example.jwttest.domain.user.service.SignInService;
import com.example.jwttest.domain.user.service.SignOutService;
import com.example.jwttest.global.security.gauth.GauthEnvironment;
import com.example.jwttest.global.security.jwt.UserInfo;
import gauth.GAuth;
import gauth.GAuthToken;
import gauth.GAuthUserInfo;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/auth/v1")
@RequiredArgsConstructor
public class AuthController {

    private final GauthEnvironment env;
    private final SignInService signInService;
    private final RefreshTokenService refreshTokenService;
    private final SignOutService signoutService;
    private final GAuth gAuth;


    @GetMapping("gauth/code")
    public ResponseEntity<TokenResDto> gauthCode(@RequestParam(value = "code") String code) throws IOException {

        GAuthToken generateToken = gAuth.generateToken(code, env.getClientId(), env.getClientSecret(), env.getRedirectUri());
        GAuthUserInfo gAuthUserInfo = gAuth.getUserInfo(generateToken.getAccessToken());

        return ResponseEntity.ok().body(signInService.execute(gAuthUserInfo));
    }

    // TODO 리다이렉트 주소가 프론트여야 함
    //  프론트에서 인증사이트로 이동 -> 인증 이후 리다이렉트 -> 프론트에서 받음 -> 서버로 코드 전송 -> 서버에서 인증 이후 ok 처리
    @GetMapping("signin") // http://localhost:8080/auth/login
    public void signIn(HttpServletResponse httpServletResponse) {
        try {
            httpServletResponse.sendRedirect(String.format("https://gauth.co.kr/login?client_id=%s&redirect_uri=%s", env.getClientId(), env.getRedirectUri()));
        } catch (IOException e) {
            throw new RuntimeException("IOException has occurred", e);
        }
    }

    @GetMapping("signout")
    public ResponseEntity<Map<String, String>> signOut() {
        UserInfo userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        signoutService.execute(userInfo.userId());
        return ResponseEntity.ok().body(Map.of("message", "User가 성공적으로 삭제되었습니다"));
    }

    @PostMapping("refresh")
    public ResponseEntity<TokenResDto> refresh(@Valid @RequestBody RefreshTokenReqDto refreshTokenReqDto) {
        return ResponseEntity.ok().body(refreshTokenService.execute(refreshTokenReqDto));
    }
}
