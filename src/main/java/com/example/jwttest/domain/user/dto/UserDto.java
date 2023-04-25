package com.example.jwttest.domain.user.dto;

import com.example.jwttest.domain.user.enums.Role;
import com.example.jwttest.domain.user.domain.User;

import java.util.UUID;

public record UserDto(UUID id, String email, String name, Integer grade,
                      Integer classNum, Integer num, String gender, String profileUrl,
                      String gAuthRole, Role role) {

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName(), user.getGrade(),
                user.getClassNum(), user.getNum(), user.getGender(), user.getProfileUrl(),
                user.getGAuthRole(), user.getRole());
    }
}
