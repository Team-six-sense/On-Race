package com.kt.onrace.domain.event.service;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.common.util.Preconditions;
import com.kt.onrace.common.response.CursorResponse;
import com.kt.onrace.domain.event.dto.EventCursorData;
import com.kt.onrace.domain.event.dto.EventDetailResponse;
import com.kt.onrace.domain.event.dto.EventInfoResponse;
import com.kt.onrace.domain.event.dto.EventListResponse;
import com.kt.onrace.domain.event.dto.EventSalesInfoResponse;
import com.kt.onrace.domain.event.dto.EventSearchRequest;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventSalesInfo;
import com.kt.onrace.domain.event.listener.QueueStatusChangedEvent;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.event.repository.EventSalesInfoRepository;

import lombok.RequiredArgsConstructor;

@ServiceLog
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

	private final EventRepository eventRepository;
	private final EventSalesInfoRepository eventSalesInfoRepository;
	private final ApplicationEventPublisher eventPublisher;

	public CursorResponse<EventListResponse> getEvents(EventSearchRequest request) {
		int fetchSize = request.getValidSize();

		return CursorResponse.ofKeyset(
			eventRepository.findVisibleEvents(request, fetchSize),
			fetchSize,
			EventListResponse::from,
			response -> EventCursorData.from(response).encode()
		);
	}

	public EventInfoResponse getEventInfo(Long eventId) {
		Event event = eventRepository.findVisibleEventOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);

		return EventInfoResponse.from(event);
	}

	public EventDetailResponse getEventDetail(Long eventId) {
		Event event = eventRepository.findVisibleEventDetailOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);

		Preconditions.validate(!event.getEventAt().isBefore(LocalDate.now().atStartOfDay()), BusinessErrorCode.EVENT_ENDED);

		EventSalesInfo salesInfo = eventSalesInfoRepository.findByEventIdOrThrow(eventId, BusinessErrorCode.SALES_INFO_NOT_FOUND);

		return EventDetailResponse.from(event, salesInfo.getDeliveryInfo(), salesInfo.getDeliveryFee());
	}

	public EventSalesInfoResponse getEventSalesInfo(Long eventId) {
		Event event = eventRepository.findVisibleEventDetailOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);

		EventSalesInfo salesInfo = eventSalesInfoRepository.findByEventIdOrThrow(eventId, BusinessErrorCode.SALES_INFO_NOT_FOUND);

		return EventSalesInfoResponse.from(salesInfo);
	}

	@Transactional
	public void enableQueue(Long eventId) {
		Event event = eventRepository.findByIdOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);
		event.enableQueue();
		eventPublisher.publishEvent(new QueueStatusChangedEvent());
	}

	@Transactional
	public void disableQueue(Long eventId) {
		Event event = eventRepository.findByIdOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);
		event.disableQueue();
		eventPublisher.publishEvent(new QueueStatusChangedEvent());
	}

	// Set<Long>의 contains는 O(1), List<Long>의 contains는 O(n)이므로 대기열 활성화 이벤트 조회 시 Set으로 반환하도록 함
	public Set<Long> getQueueEnabledEventIds() {
		return eventRepository.findAllByIsQueueTrueAndIsDeletedFalse(
		).stream()
			.map(Event::getId)
			.collect(Collectors.toSet());
	}
}
