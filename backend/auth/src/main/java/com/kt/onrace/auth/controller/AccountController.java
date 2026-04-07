package com.kt.onrace.auth.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.auth.dto.AccountMeResponse;
import com.kt.onrace.auth.dto.PassVerificationCompleteRequest;
import com.kt.onrace.auth.dto.PasswordChangeRequest;
import com.kt.onrace.auth.dto.UpdateMarketingConsentRequest;
import com.kt.onrace.auth.dto.UpdateNameRequest;
import com.kt.onrace.auth.dto.UpdateVerificationStatusRequest;
import com.kt.onrace.auth.service.AccountService;
import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.swagger.SwaggerAssistance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Account", description = "계정관리 API")
@ApiLog
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController extends SwaggerAssistance {

	private final AccountService accountService;

	@Operation(summary = "내 정보 조회", description = "로그인한 사용자의 계정 정보 조회")
	@GetMapping("/me")
	public ApiResponse<AccountMeResponse> getMyInfo(
		@RequestHeader("X-User-Id") Long userId) {

		return ApiResponse.success(accountService.getMyInfo(userId));
	}

	@Operation(summary = "이름 변경", description = "로그인한 사용자의 이름 변경")
	@PatchMapping("/me")
	public ApiResponse<Void> updateName(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody UpdateNameRequest request) {

		accountService.updateName(userId, request.name());
		return ApiResponse.success();
	}

	@Operation(summary = "마케팅 수신 동의 변경", description = "로그인한 사용자의 마케팅 수신 동의 상태 변경")
	@PatchMapping("/me/marketing-consent")
	public ApiResponse<Void> updateMarketingConsent(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody UpdateMarketingConsentRequest request) {

		accountService.updateMarketingConsent(userId, request.marketingConsent());
		return ApiResponse.success();
	}

	@Operation(summary = "비밀번호 변경 요청", description = "현재 비밀번호 확인 후 재설정 링크를 이메일로 발송 (LOCAL 계정 전용)")
	@PostMapping("/password/change-request")
	public ApiResponse<Void> requestPasswordChange(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody PasswordChangeRequest request) {

		accountService.requestPasswordChange(userId, request.currentPassword());
		return ApiResponse.success();
	}

	@Operation(summary = "PASS 본인인증 완료", description = "PASS 인증 후 이름, 성별, 생년월일을 저장하고 인증 상태를 VERIFIED로 변경")
	@PostMapping("/pass/complete")
	public ApiResponse<Void> completePassVerification(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody PassVerificationCompleteRequest request) {

		accountService.completePassVerification(userId, request.name(), request.gender(), request.birthdate());
		return ApiResponse.success();
	}

	@Operation(summary = "PASS 본인인증 해제", description = "인증 상태를 UNVERIFIED로 되돌림")
	@DeleteMapping("/pass/complete")
	public ApiResponse<Void> revokePassVerification(
		@RequestHeader("X-User-Id") Long userId) {

		accountService.revokePassVerification(userId);
		return ApiResponse.success();
	}
}
