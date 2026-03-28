package com.kt.onrace.domain.order.service;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kt.onrace.domain.order.config.OrderEntryIdMonitorProperties;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "order.entry-id.monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderEntryIdMonitor {

	private final OrderRepository orderRepository;
	private final OrderEntryIdMonitorProperties properties;

	@EventListener(ApplicationReadyEvent.class)
	public void logOnStartup() {
		logNullEntryIds("startup");
	}

	@Scheduled(fixedDelayString = "${order.entry-id.monitor.fixed-delay-ms:300000}")
	public void logOnSchedule() {
		logNullEntryIds("scheduled");
	}

	private void logNullEntryIds(String trigger) {
		long nullCount = orderRepository.countByEntryIdIsNull();
		if (nullCount == 0) {
			if ("startup".equals(trigger)) {
				log.info("orders.entry_id monitor [{}] null_count=0", trigger);
			}
			return;
		}

		int sampleSize = Math.max(properties.getSampleSize(), 1);
		List<String> samples = orderRepository.findByEntryIdIsNullOrderByCreatedAtAsc(PageRequest.of(0, sampleSize))
			.stream()
			.map(this::toSample)
			.toList();

		log.warn("orders.entry_id monitor [{}] null_count={} samples={}", trigger, nullCount, samples);
	}

	private String toSample(Order order) {
		return "id=%d,orderNumber=%s,userId=%d,eventCourseId=%d,eventPaceId=%s,createdAt=%s".formatted(
			order.getId(),
			order.getOrderNumber(),
			order.getUserId(),
			order.getEventCourseId(),
			order.getEventPaceId(),
			order.getCreatedAt()
		);
	}
}
