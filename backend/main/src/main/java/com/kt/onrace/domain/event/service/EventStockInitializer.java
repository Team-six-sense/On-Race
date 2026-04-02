package com.kt.onrace.domain.event.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.event.entity.EventStock;
import com.kt.onrace.domain.event.repository.EventPaceRepository;
import com.kt.onrace.domain.event.repository.EventStockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventStockInitializer {

	private final EventPaceRepository eventPaceRepository;
	private final EventStockRepository eventStockRepository;
	private final EventStockService eventStockService;

	/**
	 * 이벤트의 모든 페이스별 재고를 DB에서 읽어 Redis에 초기화한다(추후에 방식 변경해야함 재고 초기화)
	 */
	@ServiceLog
	public void initializeEventStock(Long eventId) {
		List<EventPace> paces = eventPaceRepository.findByEventCourseEventId(eventId);

		for (EventPace pace : paces) {
			EventStock eventStock = eventStockRepository.findByEventPaceIdOrThrow(pace.getId());
			log.debug("Initializing stock for EventPace ID: {}, Total Stock: {}, Confirmed Stock: {}",
					pace.getId(), eventStock.getTotalStock(), eventStock.getConfirmedStock());
			eventStockService.initializeStock(pace.getId(), eventStock.getTotalStock(), eventStock.getConfirmedStock());
		}
	}
}
