package com.kt.onrace.queue.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.redisson.api.RBucket;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScript;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.common.util.Preconditions;
import com.kt.onrace.common.util.RedisKeyGenerator;
import com.kt.onrace.queue.config.QueueMetrics;
import com.kt.onrace.queue.config.QueueProperties;
import com.kt.onrace.queue.dto.QueueEnterResponse;
import com.kt.onrace.queue.dto.QueueStatusResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
	private final RedissonClient redissonClient;
	private final QueueMetrics queueMetrics;
	private final QueueProperties queueProperties;

	// GET passToken → 있으면 'PASS:{token}' 반환, 없으면 ZRANK 조회 → 'WAIT:{rank}' 반환, 둘 다 없으면 nil
	private static final String STATUS_CHECK_SCRIPT =
		"local pass = redis.call('GET', KEYS[1]); " +
			"if pass then return 'PASS:' .. pass end; " +
			"local rank = redis.call('ZRANK', KEYS[2], ARGV[1]); " +
			"if rank ~= false then return 'WAIT:' .. rank end; " +
			"return nil";

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

		queueMetrics.recordEnter(paceId);

		Integer rank = waitingSet.rank(String.valueOf(userId));
		long position = (rank != null) ? rank + 1 : 1;

		log.info("[QUEUE] 대기열 진입 userId={}, paceId={}, position={}", userId, paceId, position);

		return QueueEnterResponse.of(paceId, position);
	}

	@ServiceLog
	public QueueStatusResponse getStatus(Long userId, Long paceId) {
		RScript script = redissonClient.getScript(StringCodec.INSTANCE);
		String result = script.eval(
			RScript.Mode.READ_ONLY,
			STATUS_CHECK_SCRIPT,
			RScript.ReturnType.VALUE,
			List.of(
				RedisKeyGenerator.queuePass(paceId, userId),
				RedisKeyGenerator.queueWaiting(paceId)
			),
			String.valueOf(userId)
		);

		if (result == null) {
			throw new BusinessException(BusinessErrorCode.QUEUE_NOT_FOUND);
		}

		if (result.startsWith("PASS:")) {
			String passToken = result.substring(5);
			log.info("[QUEUE] 통과 확인 userId={}, paceId={}", userId, paceId);
			return QueueStatusResponse.pass(paceId, passToken);
		}

		long position = Long.parseLong(result.substring(5)) + 1;
		long jitterMs = queueProperties.getPollJitterMs();
		long retryAfterMs = queueProperties.getPollBaseMs()
			+ (jitterMs > 0 ? ThreadLocalRandom.current().nextLong(jitterMs) : 0);
		log.debug("[QUEUE] 대기 중 userId={}, paceId={}, position={}", userId, paceId, position);
		return QueueStatusResponse.waiting(paceId, position, retryAfterMs);
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

		log.info("[QUEUE] 대기열 이탈 userId={}, paceId={}", userId, paceId);
	}
}
