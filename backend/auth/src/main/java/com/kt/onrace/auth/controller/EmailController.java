package com.kt.onrace.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.auth.dto.EmailSendCodeRequest;
import com.kt.onrace.auth.dto.EmailVerifyCodeRequest;
import com.kt.onrace.auth.service.EmailVerifyService;
import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.swagger.SwaggerAssistance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Email", description = "이메일 인증 API")
@ApiLog
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController extends SwaggerAssistance {

	private final EmailVerifyService emailVerifyService;

	@Operation(summary = "인증코드 발송", description = "입력한 이메일로 6자리 인증코드 발송 (유효시간 5분)")
	@PostMapping("/send-code")
	public ApiResponse<Void> sendCode(@Valid @RequestBody EmailSendCodeRequest request) {
		emailVerifyService.sendCode(request.email());
		return ApiResponse.success();
	}

	@Operation(summary = "인증코드 확인", description = "인증코드 검증 후 인증 완료 처리 (유효시간 10분)")
	@PostMapping("/verify-code")
	public ApiResponse<Void> verifyCode(@Valid @RequestBody EmailVerifyCodeRequest request) {
		emailVerifyService.verifyCode(request.email(), request.code());
		return ApiResponse.success();
	}
}
