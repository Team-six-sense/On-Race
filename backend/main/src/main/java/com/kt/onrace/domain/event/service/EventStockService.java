package com.kt.onrace.domain.event.service;

import java.util.List;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

import com.kt.onrace.common.util.RedisKeyGenerator;
import com.kt.onrace.domain.entry.config.EntryProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventStockService {
	private final RedissonClient redissonClient;
	private final EntryProperties entryProperties;
	private static final String RESERVE_SCRIPT =
		"if redis.call('EXISTS', KEYS[1]) == 1 then return -2 end " +
			"local stock = redis.call('DECR', KEYS[2]) " +
			"if stock < 0 then redis.call('INCR', KEYS[2]); return -1 end " +
			"redis.call('SET', KEYS[1], '1', 'EX', ARGV[1]) " +
			"return stock"; // 스크립트가 한개이고 짧아서 외부 파일로 관리하지 않고 코드 내에 직접 작성(분리해도 됩니다)

	public void initializeStock(Long paceId, int availableStock) {

		// 재고 카운터 셋팅(이 부분은 추후에 어떻게 할지 고민해봐야할듯)
		String stockKey = RedisKeyGenerator.stockKey(paceId);
		RAtomicLong stock = redissonClient.getAtomicLong(stockKey);
		stock.set(availableStock);

		String initKey = RedisKeyGenerator.stockInitializedKey(paceId);
		RBucket<String> flag = redissonClient.getBucket(initKey, StringCodec.INSTANCE);
		flag.set("1");
	}

	public long tryReserveStock(Long paceId, Long userId) {
		String reservationKey = RedisKeyGenerator.reservationKey(paceId, userId);
		String stockKey = RedisKeyGenerator.stockKey(paceId);

		RScript script = redissonClient.getScript(StringCodec.INSTANCE);

		// -2: 이미 선점된 경우, -1: 재고 부족, 0 이상: 선점 성공 (남은 재고 수)
		// eval 자체가 내부적으로 EVALSHA → NOSCRIPT fallback을 자동으로 진행해줘서 인프라 요구사항에 충족할 수 잇음!
		return script.eval(
			RScript.Mode.READ_WRITE,
			RESERVE_SCRIPT,
			RScript.ReturnType.INTEGER,
			List.of(reservationKey, stockKey),
			entryProperties.getTtlSeconds()
		);
	}

	/**
	 * (결제 확정 테스트용 임시 코드입니다 추후에 제가 삭제하겠습니다)
	 */
	public void restoreStock(Long paceId) {
		String stockKey = RedisKeyGenerator.stockKey(paceId);
		RAtomicLong stock = redissonClient.getAtomicLong(stockKey);
		stock.incrementAndGet();
	}

	/**
	 * (결제 확정 테스트용 임시 코드입니다 추후에 제가 삭제하겠습니다)
	 */
	public boolean hasReservation(Long paceId, Long userId) {
		String reservationKey = RedisKeyGenerator.reservationKey(paceId, userId);
		RBucket<String> bucket = redissonClient.getBucket(reservationKey, StringCodec.INSTANCE);
		return bucket.isExists();
	}

	/**
	 * (결제 확정 테스트용 임시 코드입니다 추후에 제가 삭제하겠습니다)
	 */
	public boolean deleteReservation(Long paceId, Long userId) {
		String reservationKey = RedisKeyGenerator.reservationKey(paceId, userId);
		RBucket<String> bucket = redissonClient.getBucket(reservationKey, StringCodec.INSTANCE);
		return bucket.delete();
	}

}
