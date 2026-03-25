package com.kt.onrace.domain.order.dto;

import java.time.LocalDateTime;

import com.kt.onrace.domain.order.entity.OrderStatus;

public record OrderSummaryDto(
	Long eventId,
	String orderNumber,
	OrderStatus orderStatus,
	LocalDateTime createdAt,
	Long finalAmount,
	String eventTitle,
	String thumbnailUrl,
	String courseName,
	String paceName
) {
}
