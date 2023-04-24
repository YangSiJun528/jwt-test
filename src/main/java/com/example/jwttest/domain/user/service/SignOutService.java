package com.example.jwttest.domain.user.service;

import com.example.jwttest.domain.token.domain.RefreshToken;
import com.example.jwttest.domain.token.repository.RefreshTokenRepository;
import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.domain.user.repository.UserRepository;
import com.example.jwttest.global.exception.error.ExpectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class SignOutService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository tokenRepository;

    public void execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException("Token과 대응되는 User가 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        RefreshToken refreshTokenEntity = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new ExpectedException("User와 대응되는 RefreshToken이 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        tokenRepository.delete(refreshTokenEntity);
        userRepository.delete(user);
    }
}
