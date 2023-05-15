package com.example.jwttest.global.security.jwt;

import com.example.jwttest.global.exception.error.ExpectedException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtManager jwtManager;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getServletPath().contains("/api/auth/v1")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7); // JWT 값 추출
        // JWT 값이 유효하다면 SecurityContextHolder에 JWT 정보를 기반으로 Authentication 객체 등록
        log.warn("JWT : {}", jwt);
        try {
            boolean isValid = jwtManager.validate(jwt);
            if (isValid) {
                UserInfo userInfo = jwtManager.extractUserInfo(jwt);
                log.warn("UserInfo : {}", userInfo);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                userInfo,
                                null,
                                userInfo.getAuthorities()
                        )
                );
            }
        } catch (JwtException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
