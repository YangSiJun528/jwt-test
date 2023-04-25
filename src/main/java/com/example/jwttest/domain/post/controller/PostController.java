package com.example.jwttest.domain.post.controller;

import com.example.jwttest.global.security.jwt.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("post")
@RequiredArgsConstructor
public class PostController {

    @GetMapping("test")
    public ResponseEntity<String> test(@AuthenticationPrincipal UserInfo userInfo) {
        return ResponseEntity.ok().body(String.valueOf(userInfo));
    }
}
