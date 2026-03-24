package com.kt.onrace.queue.processor;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
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
			RSet<String> activePaces = redissonClient.getSet(RedisKeyGenerator.queueActivePaces(), StringCodec.INSTANCE);
			Set<String> paceIds = activePaces.readAll();

			for (String paceIdStr : paceIds) {
				try {
					Long paceId = Long.parseLong(paceIdStr);
					processPaceQueue(paceId, activePaces);
				} catch (Exception e) {
					log.error("배치 처리 오류 - paceId={}, error={}", paceIdStr, e.getMessage());
				}
			}
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private void processPaceQueue(Long paceId, RSet<String> activePaces) {
		RScoredSortedSet<String> waitingSet = redissonClient.getScoredSortedSet(
			RedisKeyGenerator.queueWaiting(paceId), StringCodec.INSTANCE);
		int batchSize = queueProperties.getBatchSize();
		long passTtl = queueProperties.getPassTtlSeconds();

		Collection<String> popped = waitingSet.pollFirst(batchSize);

		if (popped == null || popped.isEmpty()) {
			// 대기열이 비었으면 활성 SET에서 제거
			if (waitingSet.isEmpty()) {
				activePaces.remove(String.valueOf(paceId));
			}
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

		// 모든 사용자를 통과시킨 뒤 대기열이 비었으면 활성 SET에서 제거
		if (waitingSet.isEmpty()) {
			activePaces.remove(String.valueOf(paceId));
		}
	}
}
