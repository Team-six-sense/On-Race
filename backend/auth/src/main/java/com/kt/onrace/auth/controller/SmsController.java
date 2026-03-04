package com.kt.onrace.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.auth.dto.SmsSendRequest;
import com.kt.onrace.auth.dto.SmsVerifyRequest;
import com.kt.onrace.auth.service.SmsVerifyService;
import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.swagger.SwaggerAssistance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "SMS Auth", description = "휴대폰 SMS 인증 API")
@ApiLog
@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController extends SwaggerAssistance {

	private final SmsVerifyService smsVerifyService;

	@Operation(summary = "인증 코드 발송", description = "휴대폰 번호로 6자리 인증 코드를 SMS로 발송합니다. (유효시간 3분)")
	@PostMapping("/send")
	public ApiResponse<Void> sendCode(@Valid @RequestBody SmsSendRequest request) {
		smsVerifyService.sendCode(request.phoneNumber());
		return ApiResponse.success();
	}

	@Operation(summary = "인증 코드 검증", description = "수신한 인증 코드를 검증합니다. 성공 시 10분간 인증 상태가 유지됩니다.")
	@PostMapping("/verify")
	public ApiResponse<Void> verifyCode(@Valid @RequestBody SmsVerifyRequest request) {
		smsVerifyService.verifyCode(request.phoneNumber(), request.code());
		return ApiResponse.success();
	}
}
