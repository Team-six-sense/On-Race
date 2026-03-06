package com.kt.onrace.domain.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.common.response.CursorResponse;
import com.kt.onrace.domain.event.dto.EventCursorData;
import com.kt.onrace.domain.event.dto.EventDetailResponse;
import com.kt.onrace.domain.event.dto.EventListResponse;
import com.kt.onrace.domain.event.dto.EventSalesInfoResponse;
import com.kt.onrace.domain.event.dto.EventSearchRequest;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventSalesInfo;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.event.repository.EventSalesInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

	private final EventRepository eventRepository;
	private final EventSalesInfoRepository eventSalesInfoRepository;

	@ServiceLog
	public CursorResponse<EventListResponse> getEvents(EventSearchRequest request) {
		int fetchSize = request.getValidSize();

		return CursorResponse.ofKeyset(
			eventRepository.findVisibleEvents(request, fetchSize),
			fetchSize,
			EventListResponse::from,
			response -> EventCursorData.from(response).encode()
		);
	}

	@ServiceLog
	public EventDetailResponse getEventDetail(Long eventId) {
		Event event = eventRepository.findVisibleEventDetailOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);

		return EventDetailResponse.from(event);
	}

	@ServiceLog
	public EventSalesInfoResponse getEventSalesInfo(Long eventId) {
		Event event = eventRepository.findVisibleEventDetailOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);

		EventSalesInfo salesInfo = eventSalesInfoRepository.findByEventIdOrThrow(eventId, BusinessErrorCode.SALES_INFO_NOT_FOUND);

		return EventSalesInfoResponse.from(salesInfo);
	}
}
