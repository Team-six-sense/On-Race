package com.onrace.api.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RateLimiterService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisScript<Long> tpsLimitScript;

    @Autowired
    private MeterRegistry meterRegistry;

    @Value("${tps.limit:100}")
    private int defaultTpsLimit;

    private static final String CONFIG_KEY = "onrace:config:tps-limit";

    // 1. 로컬 캐시 정의 (5초 TTL, 최대 1개 항목 저장)
    private Cache<String, Integer> tpsConfigCache;

    @PostConstruct
    public void init() {
        this.tpsConfigCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS) // 5초 후 만료
                .maximumSize(1)
                .build();
    }

    public boolean isAllowed(String groupId) {
        // [수정 1] 로컬 캐시 조회 시 예외 처리 완결 (HTTP 500 방지)
        int currentLimit;
        try {
            currentLimit = tpsConfigCache.get(CONFIG_KEY, key -> {
                try {
                    String dynamicLimitStr = redisTemplate.opsForValue().get(key);
                    return (dynamicLimitStr != null) ? Integer.parseInt(dynamicLimitStr) : defaultTpsLimit;
                } catch (Exception e) {
                    log.warn("[RateLimiter] Redis 설정값 로드 실패. 기본값({}) 사용: {}", defaultTpsLimit, e.getMessage());
                    return defaultTpsLimit;
                }
            });
        } catch (Exception e) {
            currentLimit = defaultTpsLimit;
        }

        try {
            String key = "tps:" + groupId + ":" + (System.currentTimeMillis() / 1000);

            Object result = redisTemplate.execute(tpsLimitScript, 
                    Collections.singletonList(key), 
                    String.valueOf(currentLimit), "2");

            boolean allowed = (result instanceof Number) && ((Number) result).longValue() == 1L;

            recordMetric(groupId, allowed ? "allowed" : "rejected");
            return allowed;

        } catch (Exception e) {
            // [수정 2] DB 연쇄 장애(Cascading Failure) 방지를 위해 Fail-Closed 정책 적용
            log.error("[RateLimiter] Redis 장애 발생 - 인프라 보호를 위해 유입 전면 차단 (Group: {})", groupId, e);
            recordMetric(groupId, "error"); 
            return false; 
        }
    }

    private void recordMetric(String groupId, String resultTag) {
        meterRegistry.counter("onrace_tps_requests_total", 
                "group", groupId, 
                "result", resultTag).increment();
    }
}