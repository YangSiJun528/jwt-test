package com.example.jwttest.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
        if (request.getServletPath().contains("/api/v1/auth")) {
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
        if (jwtManager.validate(jwt)) {
            UserInfo userInfo = jwtManager.extractUserInfo(jwt);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            userInfo,
                            null,
                            userInfo.getAuthorities()
                    )
            );
        }
        filterChain.doFilter(request, response);
    }
}