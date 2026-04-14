package com.kt.onrace.domain.event.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class StockMetrics {

	private static final String TAG_PACE_ID = "paceId";

	private final MeterRegistry registry;
	private final Set<Long> registeredPaceIds = ConcurrentHashMap.newKeySet();

	public StockMetrics(ObjectProvider<MeterRegistry> registryProvider) {
		this.registry = registryProvider.getIfAvailable();
	}

	// 재고 예약 결과 기록
	public void recordReserveResult(Long paceId, long result) {
		if (registry == null) return;
		String resultTag = switch ((int)Math.min(result, 0)) {
			case -2 -> "fail";
			case -1 -> "sold_out";
			default -> "success";
		};
		Counter.builder("stock.reserve.total")
			.tag(TAG_PACE_ID, String.valueOf(paceId))
			.tag("result", resultTag)
			.register(registry)
			.increment();
	}

	// 잔여 재고 Gauge 등록 — Prometheus scrape 시점에 supplier를 통해 Redis 조회
	public void registerStockGauges(Long paceId,
		Supplier<Number> tempStockSupplier,
		Supplier<Number> confirmStockSupplier) {
		if (registry == null) return;
		if (!registeredPaceIds.add(paceId)) return;
		String paceIdStr = String.valueOf(paceId);
		Gauge.builder("stock.remaining", tempStockSupplier)
			.tag(TAG_PACE_ID, paceIdStr).tag("type", "temp")
			.register(registry);
		Gauge.builder("stock.remaining", confirmStockSupplier)
			.tag(TAG_PACE_ID, paceIdStr).tag("type", "confirm")
			.register(registry);
	}
}
