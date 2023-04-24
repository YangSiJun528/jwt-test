package com.example.jwttest.domain.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("post")
@RequiredArgsConstructor
public class PostController {

    @GetMapping("test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok().body("HI Authenticated User");
    }
}
