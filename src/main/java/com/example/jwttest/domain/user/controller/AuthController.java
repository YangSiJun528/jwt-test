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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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


    @GetMapping("gauth/code") // /api/auth/v1/gauth/code?code=
    public ResponseEntity<TokenResDto> gauthCode(@RequestParam(value = "code") String code) throws IOException {

        GAuthToken generateToken = gAuth.generateToken(code, env.getClientId(), env.getClientSecret(), env.getRedirectUri());
        GAuthUserInfo gAuthUserInfo = gAuth.getUserInfo(generateToken.getAccessToken());

        return ResponseEntity.ok().body(signInService.execute(gAuthUserInfo));
    }

    /*

    https://gauth.co.kr/login?client_id=e5502aa30a504963a3327e84b916e1bff3eafe1987834bfd9806a01bf8c2a8cd&redirect_uri=http://localhost:3000/gauth/code

    // TODO 리다이렉트 주소가 프론트여야 함
    //  프론트에서 인증사이트로 이동 -> 인증 이후 리다이렉트 -> 프론트에서 받음 -> 서버로 코드 전송 -> 서버에서 인증 이후 ok 처리
    //  생각해보니 코드 받는거까지 프론트에서 하는게 맞음
    //  에초에 그럼 클라이언트한테 서버에 a태그로 이동하는게 노출되니까
    //  스프링에서 리다이렉트 해주는 기능을 제공하는 이유는 예전에 JSP 처럼 웹까지 함께 맡을 때 사용하다고 한거고,
    //  GCMS도 물어보니까 코드만 서버로 보내준다고 함
    @GetMapping("signin") // http://localhost:8080/api/auth/v1/signin
    public void signIn(HttpServletResponse httpServletResponse) {
        try {
            httpServletResponse.sendRedirect(String.format("https://gauth.co.kr/login?client_id=%s&redirect_uri=%s", env.getClientId(), env.getRedirectUri()));
        } catch (IOException e) {
            throw new RuntimeException("IOException has occurred", e);
        }
    }
     */

    @GetMapping("signout")
    public ResponseEntity<Map<String, String>> signOut(@AuthenticationPrincipal UserInfo userInfo) {
        log.info("{}", userInfo);
        //UserInfo userInfo = SecurityUtil.getUserInfo();
        signoutService.execute(userInfo.userId());
        return ResponseEntity.ok().body(Map.of("message", "User가 성공적으로 삭제되었습니다"));
    }

    @PostMapping("refresh")
    public ResponseEntity<TokenResDto> refresh(@Valid @RequestBody RefreshTokenReqDto refreshTokenReqDto) {
        return ResponseEntity.ok().body(refreshTokenService.execute(refreshTokenReqDto));
    }
}
