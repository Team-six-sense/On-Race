package com.kt.onrace.domain.mypage.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOverviewResponseDto;
import com.kt.onrace.domain.mypage.service.MyPageService;

import lombok.RequiredArgsConstructor;

@ApiLog
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

	@GetMapping("/entries")
	public ApiResponse<MyPageEntryListResponseDto> getEntries(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.success(myPageService.getEntries(userId));
	}

	@GetMapping("/waiting-entries")
	public ApiResponse<MyPageEntryListResponseDto> getWaitingEntries(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.success(myPageService.getWaitingEntries(userId));
	}

	@GetMapping("/orders")
	public ApiResponse<MyPageOrderListResponseDto> getOrders(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.success(myPageService.getOrders(userId));
	}

	@GetMapping("/address")
	public ApiResponse<MyPageAddressResponseDto> getAddress(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.success(myPageService.getAddress(userId));
	}
}
