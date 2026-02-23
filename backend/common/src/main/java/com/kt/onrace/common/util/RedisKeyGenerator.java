package com.kt.onrace.common.util;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyGenerator {

	public String lockKey(String resource, Long id) {
		return String.format("lock:%s:%d", resource, id);
	}
}
