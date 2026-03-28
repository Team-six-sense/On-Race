package com.onrace.api.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

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

    public boolean isAllowed(String groupId) {
        int currentLimit = defaultTpsLimit;

        try {
            // 1. Redis 설정값 조회 (실무 최적화: 1초 단위 로컬 캐싱 권장)
            String dynamicLimitStr = redisTemplate.opsForValue().get(CONFIG_KEY);
            if (dynamicLimitStr != null) {
                try {
                    currentLimit = Integer.parseInt(dynamicLimitStr);
                } catch (NumberFormatException e) {
                    log.warn("[RateLimiter] 설정값 형식 오류: {}. 기본값 사용", dynamicLimitStr);
                }
            }

            // 2. 현재 초 단위 키 생성
            String key = "tps:" + groupId + ":" + (System.currentTimeMillis() / 1000);

            // 3. Lua 스크립트 실행
            // [해결] 타입 불일치(Integer vs Long) 방지를 위해 Object로 수신
            Object result = redisTemplate.execute(tpsLimitScript, 
                    Collections.singletonList(key), 
                    String.valueOf(currentLimit), "2");

            // [해결] Number 인터페이스를 활용한 안전한 타입 변환 (NPE 및 ClassCastException 동시 방어)
            boolean allowed = (result instanceof Number) && ((Number) result).longValue() == 1L;

            // 4. 메트릭 기록 (allowed / rejected)
            recordMetric(groupId, allowed ? "allowed" : "rejected");

            return allowed;

        } catch (Exception e) {
            // [Fail-Open] Redis 장애 시 차단하지 않고 허용하며, 'error' 메트릭 기록
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