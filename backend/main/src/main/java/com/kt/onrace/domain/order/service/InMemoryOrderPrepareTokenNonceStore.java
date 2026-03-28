package com.kt.onrace.domain.order.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class InMemoryOrderPrepareTokenNonceStore implements OrderPrepareTokenNonceStore {

	// 테스트에서는 외부 Redis 없이도 nonce 재사용 방지 동작만 확인하면 된다.
	private final Map<String, Instant> usedNonces = new ConcurrentHashMap<>();

	@Override
	public synchronized boolean markAsUsed(String nonce, Duration ttl) {
		Instant now = Instant.now();
		Instant existingExpiry = usedNonces.get(nonce);
		// 아직 살아있는 nonce면 이미 checkout에 사용된 prepare token으로 본다.
		if (existingExpiry != null && existingExpiry.isAfter(now)) {
			return false;
		}

		usedNonces.put(nonce, now.plus(ttl));
		return true;
	}
}
