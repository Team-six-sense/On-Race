package com.kt.onrace.domain.mypage.dto;

import java.time.LocalDateTime;

public record MyPageOrderItemDto(
	String orderNumber,
	Long eventId,
	String status,
	String thumbnailUrl,
	String title,
	String courseName,
	String paceName,
	Long finalAmount,
	LocalDateTime orderedAt,
	LocalDateTime paymentDeadlineAt
) {
}
