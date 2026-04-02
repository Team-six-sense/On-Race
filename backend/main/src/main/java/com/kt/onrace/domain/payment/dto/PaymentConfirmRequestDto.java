package com.kt.onrace.domain.payment.dto;

public record PaymentConfirmRequestDto(
	String paymentKey,
	String orderId,
	Long amount
) {
}
