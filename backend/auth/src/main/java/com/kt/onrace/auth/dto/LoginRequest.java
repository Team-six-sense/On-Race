package com.kt.onrace.auth.dto;

import com.kt.onrace.common.logging.annotation.SensitiveLog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@SensitiveLog
public record LoginRequest(
	@NotBlank(message = "Email을 입력해 주세요.")
	@Size(max = 100)
	String email,

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	String password
) {
}
