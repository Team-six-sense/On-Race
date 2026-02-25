package com.kt.onrace.auth.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.auth.api.dto.LoginRequest;
import com.kt.onrace.auth.api.dto.LoginResponse;
import com.kt.onrace.auth.api.dto.SignupRequest;
import com.kt.onrace.auth.api.dto.SignupResponse;
import com.kt.onrace.auth.api.dto.TokenRefreshRequest;
import com.kt.onrace.auth.api.dto.TokenRefreshResponse;
import com.kt.onrace.auth.service.AuthService;
import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.swagger.SwaggerAssistance;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "인증/회원 API")
@ApiLog
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends SwaggerAssistance {

	private final AuthService authService;

	@Operation(summary = "회원가입", description = "이메일(loginId), 비밀번호, 이름, 휴대폰(선택)으로 회원가입")
	@PostMapping("/signup")
	public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
		SignupResponse response = authService.signup(request);
		return ApiResponse.success(response);
	}

	@Operation(summary = "로그인", description = "loginId/password로 로그인 후 Access Token, Refresh Token 발급")
	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ApiResponse.success(response);
	}

	@Operation(summary = "Access Token 재발급", description = "Refresh Token으로 새 Access Token 발급")
	@PostMapping("/token/refresh")
	public ApiResponse<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
		TokenRefreshResponse response = authService.refreshToken(request);
		return ApiResponse.success(response);
	}
}
