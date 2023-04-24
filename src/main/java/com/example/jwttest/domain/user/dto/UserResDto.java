package com.example.jwttest.domain.user.dto;

import com.example.jwttest.domain.user.domain.Role;
import com.example.jwttest.domain.user.domain.User;

import java.util.UUID;

public record UserResDto(UUID id, String email, String name, Integer grade,
                         Integer classNum, Integer num, String gender, String profileUrl,
                         String gAuthRole, Role role) {

    public static UserResDto from(User user) {
        return new UserResDto(user.getId(), user.getEmail(), user.getName(), user.getGrade(),
                user.getClassNum(), user.getNum(), user.getGender(), user.getProfileUrl(),
                user.getGAuthRole(), user.getRole());
    }
}
