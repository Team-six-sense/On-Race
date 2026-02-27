package com.kt.onrace.domain.order.dto;

public record CheckoutPrepareRequestDto(
	Long eventId,
	Long eventCourseId,
	Long eventPaceId
) {
}
