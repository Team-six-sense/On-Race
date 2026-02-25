package com.kt.onrace.common.util;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyGenerator {

	public String lockKey(String resource, Long id) {
		return String.format("lock:%s:%d", resource, id);
	}

	public String refreshTokenKey(Long userId) {
		return String.format("refresh_token:%d", userId);
	}

	public String blacklistKey(String jti) {
		return String.format("blacklist:jti:%s", jti);
	}
}
