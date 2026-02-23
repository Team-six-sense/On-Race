package com.kt.onrace.domain.event.dto;

import java.time.LocalDate;

import com.kt.onrace.common.request.CursorRequest;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.kt.onrace.domain.event.entity.EventType;

public record EventSearchRequest(
	EventType type,
	EventStatus status,
	Integer minDistance,
	Integer maxDistance,
	LocalDate eventStartDate,
	LocalDate eventEndDate,
	String venueAddress,
	Long cursorId,
	Integer size
) {
	public CursorRequest cursor() {
		return new CursorRequest(cursorId, size);
	}
}
