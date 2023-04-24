package com.example.jwttest.global.security.jwt;

import com.example.jwttest.domain.user.domain.Role;
import com.example.jwttest.domain.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record UserInfo(UUID userId, Role userRole) implements UserDetails {

    public static UserInfo from(User user) {
        return new UserInfo(
                user.getId(),
                user.getRole()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userId.toString()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userRole.name();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
