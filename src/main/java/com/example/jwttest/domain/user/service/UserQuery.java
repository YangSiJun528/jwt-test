package com.example.jwttest.domain.user.service;

import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.domain.user.dto.UserDto;
import com.example.jwttest.domain.user.repository.UserRepository;
import com.example.jwttest.global.exception.error.ExpectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserQuery {
    private final UserRepository userRepository;

    public UserDto execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException("Token과 대응되는 User가 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        return UserDto.from(user);
    }
}
