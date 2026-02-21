package com.kt.onrace.gateway.config;

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

			String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");

			if (ip == null || ip.isBlank()) {
				ip = java.util.Optional.ofNullable(exchange.getRequest().getRemoteAddress())
					.map(addr -> addr.getAddress().getHostAddress())
					.orElse("unknown");
			} else {
				ip = ip.split(",")[0].trim();
			}

			return Mono.just("ip:" + ip);
		};
	}

}
