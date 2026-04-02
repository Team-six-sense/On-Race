package com.kt.onrace.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kt.onrace.domain.payment.entity.PaymentStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponseDto(
	String paymentKey,
	String orderId,
	Long totalAmount,
	String method,
	PaymentStatus status
) {
}
