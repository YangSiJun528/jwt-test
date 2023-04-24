package com.example.jwttest.domain.token.domain;

import com.example.jwttest.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String token;

    @Column(unique = true, nullable = false)
    private UUID userId;
}