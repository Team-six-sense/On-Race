package com.kt.onrace.queue.processor;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kt.onrace.common.util.RedisKeyGenerator;
import com.kt.onrace.queue.config.QueueProperties;
import com.kt.onrace.queue.security.QueueTokenGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueBatchScheduler {

	private final RedissonClient redissonClient;
	private final QueueProperties queueProperties;
	private final QueueTokenGenerator queueTokenGenerator;

	private static final long LOCK_LEASE_SECONDS = 30;
	private static final String WAITING_PREFIX = "queue:waiting:";

	@Scheduled(fixedDelayString = "${queue.interval-ms}")
	public void processBatch() {
		// Redisson RLock을 사용한 분산 락
		RLock lock = redissonClient.getLock(RedisKeyGenerator.queueBatchLock());

		boolean acquired;
		try {
			acquired = lock.tryLock(0, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}

		if (!acquired) {
			return;
		}

		try {
			Iterable<String> waitingKeys = redissonClient.getKeys().getKeysByPattern(WAITING_PREFIX + "*");

			for (String waitingKey : waitingKeys) {
				try {
					Long paceId = Long.parseLong(waitingKey.substring(WAITING_PREFIX.length()));
					processPaceQueue(paceId);
				} catch (Exception e) {
					log.error("배치 처리 오류 - key={}, error={}", waitingKey, e.getMessage());
				}
			}
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private void processPaceQueue(Long paceId) {
		RScoredSortedSet<String> waitingSet = redissonClient.getScoredSortedSet(RedisKeyGenerator.queueWaiting(paceId),
			StringCodec.INSTANCE);
		int batchSize = queueProperties.getBatchSize();
		long passTtl = queueProperties.getPassTtlSeconds();

		Collection<String> popped = waitingSet.pollFirst(batchSize);

		if (popped == null || popped.isEmpty()) {
			return;
		}

		for (String userIdStr : popped) {
			if (userIdStr == null) {
				continue;
			}

			Long userId = Long.parseLong(userIdStr);
			String passKey = RedisKeyGenerator.queuePass(paceId, userId);
			String passToken = queueTokenGenerator.generatePassToken(userId, paceId);
			RBucket<String> passBucket = redissonClient.getBucket(passKey, StringCodec.INSTANCE);
			passBucket.set(passToken, passTtl, TimeUnit.SECONDS);
		}
	}
}
