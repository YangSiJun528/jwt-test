package com.example.jwttest.global.security.jwt;

import com.example.jwttest.domain.user.domain.Role;
import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.global.security.UserInfoDetails;

public record UserInfo(
        String userEmail,
        Role userRole
) {
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getEmail(),
                user.getRole()
        );
    }
}
