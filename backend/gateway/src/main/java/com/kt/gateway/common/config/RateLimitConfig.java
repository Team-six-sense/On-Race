package com.kt.gateway.common.config;

import java.util.Objects;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

	@Bean
	public KeyResolver userOrIpKeyResolver() {
		return exchange -> {
			String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

			if (userId != null && !userId.isBlank()) {
				return Mono.just("user:" + userId);
			}

			String ip = Objects.requireNonNull(
				exchange.getRequest().getRemoteAddress()
			).getAddress().getHostAddress();

			return Mono.just("ip:" + ip);
		};
	}

}
