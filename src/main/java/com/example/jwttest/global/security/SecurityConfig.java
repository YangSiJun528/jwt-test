package com.example.jwttest.global.security;

import com.example.jwttest.domain.user.enums.Role;
import com.example.jwttest.global.security.jwt.JwtAuthenticationEntryPoint;
import com.example.jwttest.global.security.jwt.JwtAuthenticationFilter;
import com.example.jwttest.global.security.jwt.LogoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final LogoutHandler logoutHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .headers(header -> header
                        .frameOptions()
                        .sameOrigin()
                )
                .authorizeHttpRequests(httpRequests -> httpRequests
                        .requestMatchers("/h2/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/rank/**").hasAnyRole(Role.ROLE_USER.getRole(), Role.ROLE_ADMIN.getRole())
                        .requestMatchers("/api/match/**").hasAnyRole(Role.ROLE_USER.getRole(), Role.ROLE_ADMIN.getRole())
                        .requestMatchers("/api/summoner/**").hasAnyRole(Role.ROLE_USER.getRole(), Role.ROLE_ADMIN.getRole())
                        .requestMatchers("/api/user/**").hasAnyRole(Role.ROLE_USER.getRole(), Role.ROLE_ADMIN.getRole())
                        .anyRequest().denyAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/v1/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                );
        return http.build();
    }
}
