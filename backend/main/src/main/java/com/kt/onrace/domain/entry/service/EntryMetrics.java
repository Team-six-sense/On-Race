package com.kt.onrace.domain.entry.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class EntryMetrics {

	private final MeterRegistry registry;

	public EntryMetrics(ObjectProvider<MeterRegistry> registryProvider) {
		this.registry = registryProvider.getIfAvailable();
	}

	// 신청/선점 결과 기록
	public void recordApply(String appType, String result) {
		if (registry == null) return;
		Counter.builder("entry.apply.total")
			.tag("appType", appType)
			.tag("result", result)
			.register(registry)
			.increment();
	}

	// 결제 확정 결과 기록
	public void recordConfirm(String appType) {
		if (registry == null) return;
		Counter.builder("entry.confirm.total")
			.tag("appType", appType)
			.register(registry)
			.increment();
	}

	// 결제 롤백 기록
	public void recordRollback(String appType) {
		if (registry == null) return;
		Counter.builder("entry.rollback.total")
			.tag("appType", appType)
			.register(registry)
			.increment();
	}
}
