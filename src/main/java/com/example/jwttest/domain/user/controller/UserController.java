package com.example.jwttest.domain.user.controller;

import com.example.jwttest.domain.user.dto.UserDto;
import com.example.jwttest.domain.user.service.SignOutService;
import com.example.jwttest.domain.user.service.UserQuery;
import com.example.jwttest.global.security.jwt.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/user/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserQuery userQuery;
    private final SignOutService signoutService;

    @GetMapping("/user")
    public ResponseEntity<UserDto> userByToken(@AuthenticationPrincipal UserInfo userInfo) {
        return ResponseEntity.ok().body(userQuery.execute(userInfo.userId()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserDto> userByID(@PathVariable UUID userId) {
        return ResponseEntity.ok().body(userQuery.execute(userId));
    }

    @GetMapping("/signout")
    public ResponseEntity<Map<String, String>> signOut(@AuthenticationPrincipal UserInfo userInfo) {
        log.info("{}", userInfo);
        signoutService.execute(userInfo.userId());
        return ResponseEntity.ok().body(Map.of("message", "User가 성공적으로 삭제되었습니다"));
    }
}
