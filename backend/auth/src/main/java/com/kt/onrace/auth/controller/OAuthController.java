package com.kt.onrace.auth.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.auth.dto.LoginResponse;
import com.kt.onrace.auth.dto.OAuthLoginRequest;
import com.kt.onrace.auth.entity.AuthProvider;
import com.kt.onrace.auth.service.OAuthService;
import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.swagger.SwaggerAssistance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "OAuth", description = "소셜 로그인 API")
@ApiLog
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController extends SwaggerAssistance {

	private final OAuthService oAuthService;

	@Operation(summary = "소셜 로그인", description = "프론트에서 소셜 인증 후 사용자 정보를 전달받아 JWT 발급 (provider: kakao, naver)")
	@PostMapping("/{provider}")
	public ApiResponse<LoginResponse> oAuthLogin(
			@PathVariable String provider,
			@Valid @RequestBody OAuthLoginRequest request) {

		AuthProvider authProvider = AuthProvider.valueOf(provider.toUpperCase());
		return ApiResponse.success(oAuthService.login(request, authProvider));
	}
}
