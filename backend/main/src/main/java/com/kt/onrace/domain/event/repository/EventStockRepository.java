package com.kt.onrace.domain.event.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.event.entity.EventStock;

public interface EventStockRepository extends JpaRepository<EventStock, Long> {

	Optional<EventStock> findByEventPaceId(Long eventPaceId);

	default EventStock findByEventPaceIdOrThrow(Long eventPaceId) {
		return findByEventPaceId(eventPaceId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.STOCK_NOT_FOUND));
	}
}
