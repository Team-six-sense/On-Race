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
        // 2. 로컬 캐시에서 먼저 조회 (없을 경우에만 Redis 접근)
        int currentLimit = tpsConfigCache.get(CONFIG_KEY, key -> {
            log.debug("[RateLimiter] Redis에서 최신 설정값 조회 수행");
            String dynamicLimitStr = redisTemplate.opsForValue().get(key);
            if (dynamicLimitStr != null) {
                try {
                    return Integer.parseInt(dynamicLimitStr);
                } catch (NumberFormatException e) {
                    log.warn("[RateLimiter] 설정값 형식 오류: {}. 기본값 사용", dynamicLimitStr);
                }
            }
            return defaultTpsLimit;
        });

        try {
            // 3. 현재 초 단위 키 생성
            String key = "tps:" + groupId + ":" + (System.currentTimeMillis() / 1000);

            // 4. Lua 스크립트 실행
            Object result = redisTemplate.execute(tpsLimitScript, 
                    Collections.singletonList(key), 
                    String.valueOf(currentLimit), "2");

            boolean allowed = (result instanceof Number) && ((Number) result).longValue() == 1L;

            recordMetric(groupId, allowed ? "allowed" : "rejected");
            return allowed;

        } catch (Exception e) {
            log.error("[RateLimiter] Redis 장애 발생 - 유입 제어 일시 해제 (Group: {})", groupId, e);
            recordMetric(groupId, "error"); 
            return true; 
        }
    }

    private void recordMetric(String groupId, String resultTag) {
        meterRegistry.counter("onrace_tps_requests_total", 
                "group", groupId, 
                "result", resultTag).increment();
    }
}