//package com.example.jwttest.domain.rank.controller;
//
//import com.example.jwttest.global.security.jwt.UserInfo;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.UUID;
//
//@Slf4j
//@Controller
//@RequestMapping("/api/rank/v1")
//@RequiredArgsConstructor
//public class RankController {
//
//    @GetMapping("ranks/by-user")
//    public ResponseEntity<> ranksByUserToken(@AuthenticationPrincipal UserInfo userInfo) {
//        return ResponseEntity.ok().body();
//    }
//
//    @GetMapping("ranks/by-user/{userId}")
//    public ResponseEntity<> ranksByUserId(@PathVariable UUID userId) {
//        return ResponseEntity.ok().body();
//    }
//
//    @GetMapping("ranks/by-category/{category}")
//    public ResponseEntity<> summonerByUserByToken(@PathVariable String category) {
//        return ResponseEntity.ok().body();
//    }
//}
