package com.kt.onrace.auth.service;

import java.time.Duration;
import java.util.Optional;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.kt.onrace.common.util.RedisKeyGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenStoreService {

	private final RedissonClient redissonClient;
	private final RedisKeyGenerator redisKeyGenerator;

	public void saveRefreshToken(Long userId, String token, long ttlMs) {
		RBucket<String> bucket = redissonClient.getBucket(redisKeyGenerator.refreshTokenKey(userId));
		bucket.set(token, Duration.ofMillis(ttlMs));
	}

	public Optional<String> getRefreshToken(Long userId) {
		RBucket<String> bucket = redissonClient.getBucket(redisKeyGenerator.refreshTokenKey(userId));
		return Optional.ofNullable(bucket.get());
	}

	public void deleteRefreshToken(Long userId) {
		redissonClient.getBucket(redisKeyGenerator.refreshTokenKey(userId)).delete();
	}

	public void blacklistJti(String jti, long remainingTtlMs) {
		RBucket<String> bucket = redissonClient.getBucket(redisKeyGenerator.blacklistKey(jti));
		bucket.set("1", Duration.ofMillis(remainingTtlMs));
	}

	public boolean isBlacklisted(String jti) {
		return redissonClient.getBucket(redisKeyGenerator.blacklistKey(jti)).isExists();
	}
}
