package com.kt.onrace.domain.order.dto;

public record CheckoutResponseDto(
	String orderNumber,
	String orderName,
	Long amount
) {
}
