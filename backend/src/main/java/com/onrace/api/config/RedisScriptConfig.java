package com.onrace.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.script.RedisScript;

@Slf4j
@Configuration
public class RedisScriptConfig {

    /**
     * [TPS 제한용 Lua 스크립트 빈 설정]
     * ClassPathResource는 빌드된 jar 내부의 resources/scripts/tps_limit.lua 파일을 읽습니다.
     * 결과 타입을 Long.class로 지정하여 Lua의 리턴값(0 또는 1)을 자바에서 안전하게 처리합니다.
     */
    @Bean
    public RedisScript<Long> tpsLimitScript() {
        try {
            Resource scriptSource = new ClassPathResource("scripts/tps_limit.lua");
            log.info("[RedisScriptConfig] TPS 제한 Lua 스크립트 로드 성공: {}", scriptSource.getFilename());
            return RedisScript.of(scriptSource, Long.class);
        } catch (Exception e) {
            log.error("[RedisScriptConfig] Lua 스크립트 파일 로드 실패! 경로를 확인하세요: scripts/tps_limit.lua", e);
            throw e;
        }
    }
}