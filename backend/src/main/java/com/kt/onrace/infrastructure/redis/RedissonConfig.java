package com.kt.onrace.infrastructure.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
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
		String protocol = redisProperties.getSsl().isEnabled() ? "rediss" : "redis";
		var uri = String.format("%s://%s", protocol, host);

		config
			.useSingleServer()
			.setAddress(uri);

		return Redisson.create(config);
	}

	@Bean
	@Profile({"dev", "prod"})
	public RedissonClient redissonSentinelClient() {
		var config = new Config();
		String protocol = redisProperties.getSsl().isEnabled() ? "rediss" : "redis";

		SentinelServersConfig sentinelConfig = config.useSentinelServers();

		sentinelConfig.setMasterName(
			redisProperties.getSentinel().getMaster()
		);

		for(String node : redisProperties.getSentinel().getNodes()) {
			String uri = String.format("%s://%s", protocol, node);
			sentinelConfig.addSentinelAddress(uri);
		}

		return Redisson.create(config);
	}
}
