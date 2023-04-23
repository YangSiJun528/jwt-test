package com.example.jwttest.domain.token.repository;

import java.util.List;
import java.util.Optional;

import com.example.jwttest.domain.token.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);
}