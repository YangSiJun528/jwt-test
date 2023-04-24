package com.example.jwttest.domain.user.service;

import com.example.jwttest.domain.token.domain.RefreshToken;
import com.example.jwttest.domain.token.repository.RefreshTokenRepository;
import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.domain.user.dto.RefreshTokenReqDto;
import com.example.jwttest.domain.user.dto.TokenResDto;
import com.example.jwttest.domain.user.repository.UserRepository;
import com.example.jwttest.global.exception.error.ExpectedException;
import com.example.jwttest.global.security.jwt.JwtManager;
import com.example.jwttest.global.security.jwt.UserInfo;
import gauth.GAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class RefreshTokenService {
    private final RefreshTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JwtManager jwtManager;

    public TokenResDto execute(RefreshTokenReqDto refreshTokenReqDto) {
        String refreshToken = refreshTokenReqDto.refreshToken();
        if(!jwtManager.validate(refreshToken)) {
            throw new ExpectedException("유효하지 않은 Token입니다", HttpStatus.BAD_REQUEST);
        }
        RefreshToken token = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ExpectedException("대체되었거나 존재하지 않는 Token입니다", HttpStatus.BAD_REQUEST));
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ExpectedException("Token과 대응되는 User가 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        String newAccessToken = jwtManager.generateToken(new HashMap<>(), UserInfo.from(user));
        String newRefreshToken = jwtManager.generateRefreshToken(UserInfo.from(user));
        RefreshToken refreshTokenEntity = tokenRepository.findByUserId(user.getId()).orElseGet(
                () -> tokenRepository.save(new RefreshToken(null, refreshToken, user.getId()))
        );
        tokenRepository.save(new RefreshToken(refreshTokenEntity.getId(), newRefreshToken, user.getId()));
        return new TokenResDto(newAccessToken, newRefreshToken);
    }
}
