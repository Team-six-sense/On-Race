package com.kt.onrace.domain.event.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.ErrorCode;
import com.kt.onrace.domain.event.dto.EventSearchRequest;
import com.kt.onrace.domain.event.entity.Event;

public interface EventRepositoryCustom {

	default Event findVisibleEventDetailOrThrow(Long id, ErrorCode errorCode) {
		return findVisibleEventDetail(id).orElseThrow(() -> new BusinessException(errorCode));
	}

	default Event findEventWithCoursesAndPacesOrThrow(Long id, ErrorCode errorCode) {
		return findEventWithCoursesAndPaces(id).orElseThrow(() -> new BusinessException(errorCode));
	}

	default Event findVisibleEventOrThrow(Long id, ErrorCode errorCode) {
		return findVisibleEvent(id).orElseThrow(() -> new BusinessException(errorCode));
	}

	List<Event> findVisibleEvents(EventSearchRequest request, int size);

	Optional<Event> findVisibleEventDetail(Long id);

	Optional<Event> findEventWithCoursesAndPaces(Long id);

	Optional<Event> findVisibleEvent(Long id);
}
