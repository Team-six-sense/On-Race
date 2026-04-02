package com.kt.onrace.domain.order.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!test")
@RequiredArgsConstructor
public class RedissonOrderPrepareTokenNonceStore implements OrderPrepareTokenNonceStore {

	// 여러 애플리케이션 인스턴스가 같은 nonce 사용 여부를 공유해야 한다.
	private static final String USED_NONCE_KEY_PREFIX = "order:prepare-token:used:";

	private final RedissonClient redissonClient;

	@Override
	public boolean markAsUsed(String nonce, Duration ttl) {
		RBucket<String> bucket = redissonClient.getBucket(USED_NONCE_KEY_PREFIX + nonce);
		// TTL 동안 키를 선점해 분산 환경에서도 1회 사용을 보장한다.
		return bucket.trySet("used", ttl.toMillis(), TimeUnit.MILLISECONDS);
	}
}
