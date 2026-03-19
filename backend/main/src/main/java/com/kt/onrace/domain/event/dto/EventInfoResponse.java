package com.kt.onrace.domain.event.dto;

import java.time.LocalDateTime;

import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventRegion;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.kt.onrace.domain.event.entity.EventType;

import lombok.Builder;

@Builder
public record EventInfoResponse(
	Long id,
	String title,
	EventType type,
	EventAppType appType,
	EventStatus status,
	LocalDateTime eventAt,
	LocalDateTime appStartAt,
	LocalDateTime appEndAt,
	EventRegion region,
	String venue
) {

	public static EventInfoResponse from(Event event) {
		return EventInfoResponse.builder()
			.id(event.getId())
			.title(event.getTitle())
			.type(event.getType())
			.appType(event.getAppType())
			.status(event.getStatus())
			.eventAt(event.getEventAt())
			.appStartAt(event.getAppStartAt())
			.appEndAt(event.getAppEndAt())
			.region(event.getRegion())
			.venue(event.getVenue())
			.build();
	}
}
