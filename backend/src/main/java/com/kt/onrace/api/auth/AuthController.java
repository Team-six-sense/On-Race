package com.kt.onrace.api.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.api.auth.dto.SignupRequest;
import com.kt.onrace.api.auth.dto.SignupResponse;
import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.swagger.SwaggerAssistance;
import com.kt.onrace.domain.auth.service.AuthService;

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
		var result = authService.signup(
			request.getEmail(),
			request.getPassword(),
			request.getName(),
			request.getMobile()
		);
		SignupResponse response = new SignupResponse(result.id(), result.email(), result.createdAt());
		return ApiResponse.success(response);
	}
}
