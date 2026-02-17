package com.kt.onrace.domain.event.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.response.CursorResponse;
import com.kt.onrace.domain.event.dto.EventDetailResponse;
import com.kt.onrace.domain.event.dto.EventSearchRequest;
import com.kt.onrace.domain.event.dto.EventListResponse;
import com.kt.onrace.domain.event.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@ApiLog
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

	private final EventService eventService;

	@GetMapping
	public ApiResponse<CursorResponse<EventListResponse>> getEvents(
		EventSearchRequest request
	) {
		return ApiResponse.success(eventService.getEvents(request));
	}

	@GetMapping("/{eventId}")
	public ApiResponse<EventDetailResponse> getEventDetail(
		@PathVariable Long eventId
	) {
		return ApiResponse.success(eventService.getEventDetail(eventId));
	}
}
