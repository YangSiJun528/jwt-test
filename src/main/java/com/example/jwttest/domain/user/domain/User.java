package com.example.jwttest.domain.user.domain;

import com.example.jwttest.domain.user.enums.Role;
import gauth.GAuthUserInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "`user`")
public class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String email;
    private String name;
    private Integer grade;
    private Integer classNum;
    private Integer num;
    private String gender;
    private String profileUrl;
    private String gAuthRole;
    private Role role;

    public static User from(GAuthUserInfo info) {
        return User.builder()
                .id(null)
                .email(info.getEmail())
                .name(info.getName())
                .grade(info.getGrade())
                .classNum(info.getClassNum())
                .num(info.getNum())
                .gender(info.getGender())
                .profileUrl(info.getProfileUrl())
                .gAuthRole(info.getRole())
                .role(Role.ROLE_USER)
                .build();
    }
}
