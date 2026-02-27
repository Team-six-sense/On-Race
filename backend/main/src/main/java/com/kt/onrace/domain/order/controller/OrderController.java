package com.kt.onrace.domain.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.order.dto.CheckoutPrepareRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutPrepareResponseDto;
import com.kt.onrace.domain.order.dto.CheckoutRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutResponseDto;
import com.kt.onrace.domain.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	@PostMapping("/checkout-info")
	public ApiResponse<CheckoutPrepareResponseDto> checkoutPrepare(
		@RequestBody CheckoutPrepareRequestDto request,
		@RequestHeader("X-User-Id") Long userId) {

		CheckoutPrepareResponseDto response = orderService.getCheckoutPrepareInfo(request, userId);

		return ApiResponse.success(response);
	}

	@PostMapping("/checkout")
	public ApiResponse<CheckoutResponseDto> checkout(
		@RequestBody CheckoutRequestDto request,
		@RequestHeader("X-User-Id") Long userId) {

		CheckoutResponseDto response = orderService.checkout(request, userId);
		return ApiResponse.success(response);
	}

}
