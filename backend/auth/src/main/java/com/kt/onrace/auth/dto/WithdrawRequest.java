package com.kt.onrace.auth.dto;

import com.kt.onrace.common.logging.annotation.SensitiveLog;

import jakarta.validation.constraints.NotBlank;

@SensitiveLog
public record WithdrawRequest(
	@NotBlank(message = "비밀번호를 입력해 주세요.")
	String password
) {
}
