package com.example.jwttest.global.security.util;

import com.example.jwttest.global.security.jwt.UserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *  보안과 인증 관련 유틸리티 메소드를 제공합니다 <br>
 *  Controller Layer에서만 사용되어야 합니다.
 */
@Deprecated
public class SecurityUtil {
    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static UserInfo getUserInfo() {
        return (UserInfo) getAuthentication().getPrincipal();
    }
}
