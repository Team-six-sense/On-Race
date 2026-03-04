package com.kt.onrace.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.auth.dto.PasswordResetConfirmRequest;
import com.kt.onrace.auth.dto.PasswordResetRequest;
import com.kt.onrace.auth.service.PasswordResetService;
import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.swagger.SwaggerAssistance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Password Reset", description = "비밀번호 재설정 API")
@ApiLog
@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordResetController extends SwaggerAssistance {

	private final PasswordResetService passwordResetService;

	@Value("${app.password-reset.base-url}")
	private String resetBaseUrl;

	@Operation(summary = "비밀번호 재설정 요청", description = "가입된 이메일로 재설정 링크 발송 (15분 유효, 5회/24시간 제한)")
	@PostMapping("/reset-request")
	public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
		passwordResetService.requestPasswordReset(request.email(), resetBaseUrl);
		return ApiResponse.success();
	}

	@Operation(summary = "비밀번호 재설정 링크 인증", description = "재설정 링크 내 토큰 검증 (1회성)")
	@GetMapping("/reset-verify")
	public ApiResponse<Void> verifyResetToken(@RequestParam String token) {
		passwordResetService.verifyResetToken(token);
		return ApiResponse.success();
	}

	@Operation(summary = "비밀번호 변경", description = "재설정 인증 완료 후 새 비밀번호 저장 및 기존 세션 만료")
	@PostMapping("/reset")
	public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
		passwordResetService.resetPassword(request.token(), request.newPassword());
		return ApiResponse.success();
	}
}
