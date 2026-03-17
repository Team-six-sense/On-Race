package com.kt.onrace.domain.event.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.response.CursorResponse;
import com.kt.onrace.domain.event.dto.EventDetailResponse;
import com.kt.onrace.domain.event.dto.EventInfoResponse;
import com.kt.onrace.domain.event.dto.EventListResponse;
import com.kt.onrace.domain.event.dto.EventSearchRequest;
import com.kt.onrace.domain.event.dto.EventSalesInfoResponse;
import com.kt.onrace.domain.event.service.EventService;
import com.kt.onrace.domain.event.service.EventStockInitializer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@ApiLog
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

	private final EventService eventService;
	private final EventStockInitializer eventStockInitializer;

	@GetMapping
	public ApiResponse<CursorResponse<EventListResponse>> getEvents(
		@Valid EventSearchRequest request
	) {
		return ApiResponse.success(eventService.getEvents(request));
	}

	@GetMapping("/{eventId}")
	public ApiResponse<EventDetailResponse> getEventDetail(
		@PathVariable Long eventId
	) {
		return ApiResponse.success(eventService.getEventDetail(eventId));
	}

	@GetMapping("/{eventId}/info")
	public ApiResponse<EventInfoResponse> getEventInfo(
		@PathVariable Long eventId
	) {
		return ApiResponse.success(eventService.getEventInfo(eventId));
	}

	@GetMapping("/{eventId}/sales-info")
	public ApiResponse<EventSalesInfoResponse> getEventSalesInfo(
		@PathVariable Long eventId
	) {
		return ApiResponse.success(eventService.getEventSalesInfo(eventId));
	}

	@PostMapping("/{eventId}/stock/init")
	public ApiResponse<Void> initializeEventStock(
		@PathVariable Long eventId
	) {
		eventStockInitializer.initializeEventStock(eventId);
		return ApiResponse.success();
	}
}
