package com.kt.onrace.domain.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.order.dto.CheckoutPrepareRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutPrepareResponseDto;
import com.kt.onrace.domain.order.dto.CheckoutRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutResponseDto;
import com.kt.onrace.domain.order.dto.OrderDetailResponseDto;
import com.kt.onrace.domain.order.dto.OrderListResponseDto;
import com.kt.onrace.domain.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	@GetMapping
	public ApiResponse<OrderListResponseDto> getOrders(
		@RequestParam("tab") String tab,
		@RequestHeader("X-User-Id") Long userId) {

		OrderListResponseDto response = orderService.getOrders(tab, userId);
		return ApiResponse.success(response);
	}

	@GetMapping("/{orderNumber}")
	public ApiResponse<OrderDetailResponseDto> getOrderDetail(
		@PathVariable String orderNumber,
		@RequestHeader("X-User-Id") Long userId) {

		OrderDetailResponseDto response = orderService.getOrderDetail(orderNumber, userId);
		return ApiResponse.success(response);
	}

	@PostMapping("/{orderNumber}/confirm")
	public ApiResponse<Void> confirmPayment(
		@PathVariable String orderNumber,
		@RequestHeader("X-User-Id") Long userId) {

		orderService.confirmPayment(orderNumber, userId);
		return ApiResponse.success();
	}

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
