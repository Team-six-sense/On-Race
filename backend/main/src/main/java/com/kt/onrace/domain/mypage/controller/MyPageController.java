package com.kt.onrace.domain.mypage.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageAccountResponse;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryFilter;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderDetailResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOverviewResponseDto;
import com.kt.onrace.domain.mypage.service.MyPageService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

/**
 * 마이페이지 화면에서 필요한 조회 API를 제공하는 REST 컨트롤러이다.
 * 개요, 신청 내역, 대기 신청 내역, 주문 목록 및 상세, 기본 배송지 조회를 담당한다.
 */
@ApiLog
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

	private final MyPageService myPageService;

	@GetMapping
	public ApiResponse<MyPageOverviewResponseDto> getOverview(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.success(myPageService.getOverview(userId));
	}

	@GetMapping("/account")
	public ApiResponse<MyPageAccountResponse> getAccount(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.success(myPageService.getAccount(userId));
	}

	@GetMapping("/entries")
	public ApiResponse<MyPageApplicationHistoryResponseDto> getEntries(
		@RequestHeader("X-User-Id") Long userId,
		@RequestParam(defaultValue = "ALL") String filter,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
	) {
		return ApiResponse.success(
			myPageService.getApplicationHistory(userId, MyPageApplicationHistoryFilter.from(filter), page, size)
		);
	}

	@GetMapping("/waiting-entries")
	public ApiResponse<MyPageEntryListResponseDto> getWaitingEntries(
		@RequestHeader("X-User-Id") Long userId,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
	) {
		return ApiResponse.success(myPageService.getWaitingEntries(userId, page, size));
	}

	@GetMapping("/orders")
	public ApiResponse<MyPageOrderListResponseDto> getOrders(
		@RequestHeader("X-User-Id") Long userId,
		@RequestParam(defaultValue = "ALL") String tab,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
	) {
		return ApiResponse.success(myPageService.getOrders(userId, tab, page, size));
	}

	@GetMapping("/orders/{orderNumber}")
	public ApiResponse<MyPageOrderDetailResponseDto> getOrderDetail(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable String orderNumber
	) {
		return ApiResponse.success(myPageService.getOrderDetail(userId, orderNumber));
	}

	@GetMapping("/address")
	public ApiResponse<MyPageAddressResponseDto> getAddress(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.success(myPageService.getAddress(userId));
	}
}
