package com.kt.onrace.domain.loadtest.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.loadtest.dto.LoadTestConfirmRequest;
import com.kt.onrace.domain.loadtest.dto.LoadTestSetupRequest;
import com.kt.onrace.domain.loadtest.dto.LoadTestSetupResponse;
import com.kt.onrace.domain.loadtest.service.LoadTestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 부하테스트 데이터 셋업 API
 * local 프로필에서만 활성화
 */
@Profile("local")
@ApiLog
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/load-test")
public class LoadTestController {

	private final LoadTestService loadTestService;

	/**
	 * 부하테스트 전체 데이터 셋업
	 * - 유저 3만명 (멱등)
	 * - 이벤트 3개 + 코스 + 페이스 + 재고 + 판매정보 (삭제 후 재등록)
	 * - Redis flushDB + 재고 초기화 + 대기열 활성화
	 */
	@PostMapping("/setup")
	public ApiResponse<LoadTestSetupResponse> setup(
		@RequestBody @Valid LoadTestSetupRequest request
	) {
		return ApiResponse.success(loadTestService.setup(request));
	}

	/**
	 * 테스트 전용 예약 확정 (Toss 결제 우회)
	 * EntryService.confirmReservation()을 직접 호출하여
	 * RESERVED → APPLIED 상태 전환 + 재고 확정 처리
	 */
	@PostMapping("/confirm-reservation")
	public ApiResponse<Void> confirmReservation(
		@RequestHeader("X-User-Id") Long userId,
		@RequestBody @Valid LoadTestConfirmRequest request
	) {
		loadTestService.confirmReservation(userId, request.paceId());
		return ApiResponse.success(null);
	}
}
