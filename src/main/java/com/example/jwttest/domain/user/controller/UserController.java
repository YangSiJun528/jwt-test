package com.example.jwttest.domain.user.controller;

import com.example.jwttest.domain.user.dto.UserResDto;
import com.example.jwttest.domain.user.service.UserQuery;
import com.example.jwttest.global.security.jwt.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/api/user/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserQuery userQuery;

    @GetMapping("user")
    public ResponseEntity<UserResDto> user() {
        UserInfo userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok().body(userQuery.execute(userInfo.userId()));
    }
}
