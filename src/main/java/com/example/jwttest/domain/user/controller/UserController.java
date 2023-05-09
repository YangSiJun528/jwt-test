package com.example.jwttest.domain.user.controller;

import com.example.jwttest.domain.user.dto.UserDto;
import com.example.jwttest.domain.user.service.UserQuery;
import com.example.jwttest.global.security.jwt.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/api/user/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserQuery userQuery;

    @GetMapping("user")
    public ResponseEntity<UserDto> userByToken(@AuthenticationPrincipal UserInfo userInfo) {
        return ResponseEntity.ok().body(userQuery.execute(userInfo.userId()));
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<UserDto> userByID(@PathVariable UUID userId) {
        return ResponseEntity.ok().body(userQuery.execute(userId));
    }
}
