package com.onrace.api.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class RateLimiterService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisScript<Long> tpsLimitScript;

    @Autowired
    private MeterRegistry meterRegistry; // Prometheus 메트릭 기록용

    public boolean isAllowed(String groupId, int maxTps) {
        String key = "tps:" + groupId + ":" + (System.currentTimeMillis() / 1000);
        
        try {
            // Lua 스크립트 실행 (키, 최대TPS, 만료시간 2초)
            Long result = redisTemplate.execute(tpsLimitScript, 
                    Collections.singletonList(key), 
                    String.valueOf(maxTps), "2");
            
            boolean allowed = (result != null && result == 1L);

            // [핵심] KEDA와 Prometheus가 읽어갈 메트릭 기록
            // result="allowed" 또는 "rejected" 태그를 붙여서 카운팅
            String statusTag = allowed ? "allowed" : "rejected";
            meterRegistry.counter("onrace_tps_requests_total", "group", groupId, "result", statusTag).increment();

            return allowed;

        } catch (Exception e) {
            // Fail-Open: Redis 장애 시 일단 통과시키고 에러 로그 남김
            log.error("[RateLimiter] Redis 연결 오류 - 유입 제어 일시 해제 (Group: {})", groupId, e);
            return true; 
        }
    }
}