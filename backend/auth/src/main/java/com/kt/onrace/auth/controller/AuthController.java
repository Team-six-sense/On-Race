package com.kt.onrace.auth.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.auth.dto.LoginRequest;
import com.kt.onrace.auth.dto.LoginResponse;
import com.kt.onrace.auth.dto.SignupRequest;
import com.kt.onrace.auth.dto.SignupResponse;
import com.kt.onrace.auth.dto.TokenRefreshRequest;
import com.kt.onrace.auth.dto.TokenRefreshResponse;
import com.kt.onrace.auth.dto.WithdrawRequest;
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
@RequestMapping
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

	@Operation(summary = "로그아웃", description = "Access Token 블랙리스트 등록 및 Refresh Token 삭제")
	@PostMapping("/logout")
	public ApiResponse<Void> logout(
		@RequestHeader("X-User-Id") Long userId,
		@RequestHeader("Authorization") String authorization) {

		authService.logout(userId, authorization.substring(7));
		return ApiResponse.success();
	}

	@Operation(summary = "회원탈퇴", description = "비밀번호 재확인 후 계정 삭제 처리 및 토큰 무효화")
	@DeleteMapping("/account")
	public ApiResponse<Void> withdraw(
		@RequestHeader("X-User-Id") Long userId,
		@RequestHeader("Authorization") String authorization,
		@Valid @RequestBody WithdrawRequest request) {

		authService.withdraw(userId, authorization.substring(7), request);
		return ApiResponse.success();
	}
}
