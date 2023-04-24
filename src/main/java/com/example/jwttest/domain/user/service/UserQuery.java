package com.example.jwttest.domain.user.service;

import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.domain.user.dto.UserResDto;
import com.example.jwttest.domain.user.repository.UserRepository;
import com.example.jwttest.global.exception.error.ExpectedException;
import com.example.jwttest.global.security.jwt.JwtManager;
import com.example.jwttest.global.security.jwt.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQuery {
    private final UserRepository userRepository;
    private final JwtManager jwtManager;

    public UserResDto execute(String accessToken) {
        UserInfo userInfo = jwtManager.extractUserInfo(accessToken);
        User user = userRepository.findById(userInfo.userId())
                .orElseThrow(() -> new ExpectedException("Token과 대응되는 User가 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        return UserResDto.from(user);
    }
}
