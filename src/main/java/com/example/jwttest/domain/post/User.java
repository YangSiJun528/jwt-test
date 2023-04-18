package com.example.jwttest.domain.post;

import gauth.GAuthUserInfo;

import java.util.Map;

public class User extends GAuthUserInfo {
    public User(Map<String, Object> map) {
        super(map);
    }
}
