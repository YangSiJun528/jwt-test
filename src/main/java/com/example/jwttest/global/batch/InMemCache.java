package com.example.jwttest.global.batch;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InMemCache {  // TODO 나중에 레디스든 어디든 캐시로 빼기 + 메모리 위로 띄울꺼, 그리고 성능 생각해면 Map으로 쓰는게 좋음
    private static final Set<String> CACHE = Collections.synchronizedSet(new HashSet<>());

    private InMemCache() {}

    public static Set<String> getInstance() {
        return CACHE;
    }

    public static void clear() {
        CACHE.clear();
    }
}
