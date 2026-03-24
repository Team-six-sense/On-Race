package com.kt.onrace.queue.service;

import org.redisson.api.RBucket;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.common.util.Preconditions;
import com.kt.onrace.common.util.RedisKeyGenerator;
import com.kt.onrace.queue.dto.QueueEnterResponse;
import com.kt.onrace.queue.dto.QueueStatusResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
	private final RedissonClient redissonClient;

	@ServiceLog
	public QueueEnterResponse enter(Long userId, Long paceId) {
		// 이미 통과한 사용자 재진입 차단
		RBucket<String> passBucket = redissonClient.getBucket(
			RedisKeyGenerator.queuePass(paceId, userId), StringCodec.INSTANCE);
		Preconditions.validate(!passBucket.isExists(), BusinessErrorCode.QUEUE_ALREADY_ENTERED);

		// 대기열 진입 (ZADD NX — 기존 사용자 score 갱신 방지)
		RScoredSortedSet<String> waitingSet = redissonClient.getScoredSortedSet(
			RedisKeyGenerator.queueWaiting(paceId), StringCodec.INSTANCE);
		boolean added = waitingSet.addIfAbsent(System.currentTimeMillis(), String.valueOf(userId));
		Preconditions.validate(added, BusinessErrorCode.QUEUE_ALREADY_ENTERED);

		// 활성 paceId SET에 등록 (SADD — 이미 존재하면 무시)
		RSet<String> activePaces = redissonClient.getSet(RedisKeyGenerator.queueActivePaces(), StringCodec.INSTANCE);
		activePaces.add(String.valueOf(paceId));

		Integer rank = waitingSet.rank(String.valueOf(userId));
		long position = (rank != null) ? rank + 1 : 1;

		return QueueEnterResponse.of(paceId, position);
	}

	@ServiceLog
	public QueueStatusResponse getStatus(Long userId, Long paceId) {
		RBucket<String> passBucket = redissonClient.getBucket(
			RedisKeyGenerator.queuePass(paceId, userId), StringCodec.INSTANCE);
		String passToken = passBucket.get();

		if (passToken != null) {
			return QueueStatusResponse.pass(paceId, passToken);
		}

		RScoredSortedSet<String> waitingSet = redissonClient.getScoredSortedSet(
			RedisKeyGenerator.queueWaiting(paceId), StringCodec.INSTANCE);
		Integer rank = waitingSet.rank(String.valueOf(userId));

		if (rank != null) {
			return QueueStatusResponse.waiting(paceId, (long) rank + 1);
		}

		throw new BusinessException(BusinessErrorCode.QUEUE_NOT_FOUND);
	}

	@ServiceLog
	public void leave(Long userId, Long paceId) {
		RScoredSortedSet<String> waitingSet = redissonClient.getScoredSortedSet(RedisKeyGenerator.queueWaiting(paceId),
			StringCodec.INSTANCE);
		boolean removedFromWaiting = waitingSet.remove(String.valueOf(userId));

		RBucket<String> passBucket = redissonClient.getBucket(RedisKeyGenerator.queuePass(paceId, userId),
			StringCodec.INSTANCE);
		boolean deletedPass = passBucket.delete();

		Preconditions.validate(removedFromWaiting || deletedPass, BusinessErrorCode.QUEUE_NOT_FOUND);
	}
}
