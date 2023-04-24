package com.example.jwttest.domain.user.service;

import com.example.jwttest.domain.token.domain.RefreshToken;
import com.example.jwttest.domain.token.repository.RefreshTokenRepository;
import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.domain.user.dto.TokenResDto;
import com.example.jwttest.domain.user.repository.UserRepository;
import com.example.jwttest.global.security.jwt.JwtManager;
import com.example.jwttest.global.security.jwt.UserInfo;
import gauth.GAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class SignInService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository tokenRepository;
    private final JwtManager jwtManager;

    public TokenResDto execute(GAuthUserInfo gAuthUserInfo) {
        User user = userRepository.findByEmail(gAuthUserInfo.getEmail()).orElseGet(
                () -> userRepository.save(User.from(gAuthUserInfo))
        );
        UserInfo userInfo = UserInfo.from(user);
        String accessToken = jwtManager.generateToken(new HashMap<>(), userInfo);
        String refreshToken = jwtManager.generateRefreshToken(userInfo);
        RefreshToken refreshTokenEntity = tokenRepository.findByUserId(user.getId()).orElseGet(
                () -> tokenRepository.save(new RefreshToken(null, refreshToken, user.getId()))
        );
        tokenRepository.save(new RefreshToken(refreshTokenEntity.getId(), refreshToken, user.getId()));
        return new TokenResDto(accessToken, refreshToken);
    }
}
