package com.example.jwttest.global.batch;

import com.example.jwttest.domain.statistics.domain.Statistics;
import com.example.jwttest.domain.summoner.domain.Summoner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InMemCacheStatistics {  // TODO 나중에 레디스든 어디든 캐시로 빼기 + 메모리 위로 띄울꺼 + Map으로
    private static final Set<Statistics> CACHE = Collections.synchronizedSet(new HashSet<>());

    private InMemCacheStatistics() {}

    public static Set<Statistics> getInstance() {
        return CACHE;
    }

    public static void clear() {
        CACHE.clear();
    }
}
