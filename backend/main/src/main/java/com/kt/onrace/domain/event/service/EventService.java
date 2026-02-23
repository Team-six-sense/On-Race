package com.kt.onrace.domain.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.common.response.CursorResponse;
import com.kt.onrace.domain.event.dto.EventDetailResponse;
import com.kt.onrace.domain.event.dto.EventListResponse;
import com.kt.onrace.domain.event.dto.EventSearchRequest;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@ServiceLog
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

	private final EventRepository eventRepository;

	public CursorResponse<EventListResponse> getEvents(EventSearchRequest request) {
		int fetchSize = request.cursor().getValidSize();

		return CursorResponse.of(
			eventRepository.findVisibleEvents(request, fetchSize),
			fetchSize,
			EventListResponse::from,
			EventListResponse::id
		);
	}

	public EventDetailResponse getEventDetail(Long eventId) {
		Event event = eventRepository.findVisibleEventDetail(eventId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.EVENT_NOT_FOUND));

		return EventDetailResponse.from(event);
	}
}
