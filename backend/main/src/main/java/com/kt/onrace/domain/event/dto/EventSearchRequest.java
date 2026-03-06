package com.kt.onrace.domain.event.dto;

import java.time.LocalDate;

import com.kt.onrace.domain.event.entity.EventRegion;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

public record EventSearchRequest(
	EventType type,
	EventAppType appType,
	EventStatus status,

	@PositiveOrZero(message = "최소 거리는 -1보다 커야 합니다.")
	Integer minDistance,

	@Max(value = 42195, message = "최대 거리는 42.195km 이하이어야 합니다.")
	Integer maxDistance,
	LocalDate eventStartDate,
	LocalDate eventEndDate,
	EventRegion region,
	String keyword,

	String cursor,

	@Min(value = 1, message = "size는 최소 1 이상이어야 합니다.")
	@Max(value = 50, message = "size는 최대 50 이하만 가능합니다.")
	Integer size
) {

	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 50;

	public int getValidSize() {
		if (size == null || size < 1) {
			return DEFAULT_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}

	public EventCursorData decodeCursor() {
		if (cursor == null || cursor.isBlank()) {
			return null;
		}
		return EventCursorData.decode(cursor);
	}
}
