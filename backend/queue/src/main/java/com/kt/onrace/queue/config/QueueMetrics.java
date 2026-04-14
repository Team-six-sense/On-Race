package com.kt.onrace.queue.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class QueueMetrics {

	private static final String TAG_PACE_ID = "paceId";

	private final MeterRegistry registry;
	private final ConcurrentHashMap<Long, AtomicLong> waitingSizeMap = new ConcurrentHashMap<>();

	public QueueMetrics(ObjectProvider<MeterRegistry> registryProvider) {
		this.registry = registryProvider.getIfAvailable();
	}


	// 대기열 진입 성공 시 호출
	public void recordEnter(Long paceId) {
		if (registry == null) return;
		Counter.builder("queue.enter.total")
			.tag(TAG_PACE_ID, String.valueOf(paceId))
			.register(registry)
			.increment();
	}

	// 배치 통과 인원 기록
	public void recordPass(Long paceId, int count) {
		if (registry == null || count <= 0) return;
		Counter.builder("queue.pass.total")
			.tag(TAG_PACE_ID, String.valueOf(paceId))
			.register(registry)
			.increment(count);
	}

	// 배치 처리 시간 측정 시작
	public Timer.Sample startBatchTimer() {
		if (registry == null) return null;
		return Timer.start(registry);
	}

	// 배치 처리 시간 측정 종료
	public void stopBatchTimer(Timer.Sample sample) {
		if (sample == null || registry == null) return;
		sample.stop(Timer.builder("queue.batch.duration").register(registry));
	}

	// 대기열 크기 Gauge 갱신 — paceId별 최초 호출 시 자동 등록
	public void updateWaitingSize(Long paceId, long size) {
		if (registry == null) return;
		AtomicLong gauge = waitingSizeMap.computeIfAbsent(paceId, id -> {
			AtomicLong value = new AtomicLong(0);
			Gauge.builder("queue.waiting.size", value, AtomicLong::doubleValue)
				.tag(TAG_PACE_ID, String.valueOf(id))
				.register(registry);
			return value;
		});
		gauge.set(size);
	}
}
