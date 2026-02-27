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

	public String emailVerifyCodeKey(String email) {
		return String.format("email:verify_code:%s", email);
	}

	public String emailVerifiedKey(String email) {
		return String.format("email:verified:%s", email);
	}
}
