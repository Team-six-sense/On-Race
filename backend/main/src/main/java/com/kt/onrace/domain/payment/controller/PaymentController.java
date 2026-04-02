package com.kt.onrace.domain.payment.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.payment.dto.PaymentConfirmRequestDto;
import com.kt.onrace.domain.payment.dto.TossPaymentResponseDto;
import com.kt.onrace.domain.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentService paymentService;

	@ApiLog
	@PostMapping("/confirm")
	public ApiResponse<TossPaymentResponseDto> confirmPayment(
		@RequestHeader("X-User-Id") Long userId,
		@RequestBody PaymentConfirmRequestDto request
	) {
		return ApiResponse.success(paymentService.confirmPayment(userId, request));
	}
	
}
