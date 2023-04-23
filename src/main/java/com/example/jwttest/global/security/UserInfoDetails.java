package com.example.jwttest.global.security;

import com.example.jwttest.domain.user.domain.User;
import com.example.jwttest.domain.user.enums.Authority;
import com.example.jwttest.global.security.jwt.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoDetails implements UserDetails {

    private final UserInfo userInfo;

    public UserInfoDetails(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userInfo.userRole().name()));
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return userInfo.userEmail(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}