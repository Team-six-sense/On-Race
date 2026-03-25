package com.onrace.api.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 추가
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

    // [수정] application.yml에서 기본값 100을 읽어옴
    @Value("${tps.limit:100}")
    private int defaultTpsLimit;

    // Redis에 저장할 설정 키 이름
    private static final String CONFIG_KEY = "onrace:config:tps-limit";

    /**
     * @param groupId 그룹 ID (g1, g2 등)
     * 인자에서 maxTps를 제거하여 내부적으로 동적 결정하도록 변경
     */
    public boolean isAllowed(String groupId) {
        // 1. Redis에서 실시간 TPS 제한 설정값 조회
        String dynamicLimitStr = redisTemplate.opsForValue().get(CONFIG_KEY);
        
        // 2. Redis 값이 있으면 우선 적용, 없으면 yml 설정(100) 적용
        int currentLimit = (dynamicLimitStr != null) ? Integer.parseInt(dynamicLimitStr) : defaultTpsLimit;

        String key = "tps:" + groupId + ":" + (System.currentTimeMillis() / 1000);
        
        try {
            // 3. 결정된 currentLimit으로 Lua 스크립트 실행
            Long result = redisTemplate.execute(tpsLimitScript, 
                    Collections.singletonList(key), 
                    String.valueOf(currentLimit), "2");
            
            boolean allowed = (result != null && result == 1L);

            // 4. Prometheus 메트릭 기록 (KEDA 스케일링 소스)
            String statusTag = allowed ? "allowed" : "rejected";
            meterRegistry.counter("onrace_tps_requests_total", 
                    "group", groupId, 
                    "result", statusTag).increment();

            return allowed;

        } catch (Exception e) {
            log.error("[RateLimiter] Redis 연결 오류 - 유입 제어 일시 해제 (Group: {})", groupId, e);
            return true; // Fail-Open
        }
    }
}