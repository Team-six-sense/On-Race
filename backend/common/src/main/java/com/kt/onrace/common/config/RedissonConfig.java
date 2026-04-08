package com.kt.onrace.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("!test")
@Slf4j
@RequiredArgsConstructor
@Configuration
public class RedissonConfig {

    private final RedisProperties redisProperties;

    @Bean
    @Profile("local")
    public RedissonClient redissonSingleClient() {
        var config = new Config();
        String host = redisProperties.getHost() + ":" + redisProperties.getPort();
        // local 환경 SSL 여부에 따른 프로토콜 설정
        String protocol = (redisProperties.getSsl() != null && redisProperties.getSsl().isEnabled()) ? "rediss" : "redis";
        var uri = String.format("%s://%s", protocol, host);

        config
            .useSingleServer()
            .setAddress(uri)
            .setPassword(redisProperties.getPassword());

        return Redisson.create(config);
    }

    @Bean
    @Profile({"dev", "prod"})
    public RedissonClient redissonProdClient() {
        var config = new Config();
        
        // dev, prod 환경에서는 Stunnel(사이드카)을 통해 AWS ElastiCache에 접속합니다.
        // AWS가 고가용성(HA)을 보장하므로, 앱은 Stunnel의 단일 창구(127.0.0.1:6379)만 바라봅니다.
        String host = redisProperties.getHost() + ":" + redisProperties.getPort();
        
        // Stunnel이 TLS 암호화를 대신 처리해주므로, 앱과 Stunnel 구간은 일반 redis:// 프로토콜을 사용합니다.
        String uri = String.format("redis://%s", host);

        config
            .useSingleServer()
            .setAddress(uri)
            .setPassword(redisProperties.getPassword());

        log.info("[RedissonConfig] Configured for PROD/DEV with Single Server Mode via Stunnel: {}", uri);

        return Redisson.create(config);
    }
}