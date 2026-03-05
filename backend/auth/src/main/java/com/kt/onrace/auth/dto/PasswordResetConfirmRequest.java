package com.kt.onrace.auth.dto;

import com.kt.onrace.common.logging.annotation.SensitiveLog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@SensitiveLog
public record PasswordResetConfirmRequest(
	@NotBlank String token,

	@NotBlank
	@Size(min = 10, max = 16, message = "비밀번호는 10~16자여야 합니다.")
	@Pattern(
		regexp = "^(?:(?=.*[A-Za-z])(?=.*\\d)|(?=.*[A-Za-z])(?=.*[!@#$%^&*])|(?=.*\\d)(?=.*[!@#$%^&*])).{10,16}$",
		message = "비밀번호는 영문, 숫자, 특수문자(!@#$%^&*) 중 2가지 이상 조합이어야 합니다."
	)
	String newPassword
) {
}
