package com.kt.onrace.auth.dto;

import com.kt.onrace.common.logging.annotation.SensitiveLog;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@SensitiveLog
public record SignupRequest(
		@NotBlank(message = "이메일을 입력해 주세요.") @Email(message = "올바른 이메일 형식이 아닙니다.") @Size(max = 100) String email,

		@NotBlank(message = "비밀번호를 입력해 주세요.") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^])[A-Za-z\\d!@#$%^]{8,}$", message = "비밀번호는 대소문자, 숫자, 특수문자(!@#$%^)를 포함한 8자 이상이어야 합니다.") String password,

		@NotBlank(message = "핸드폰 번호를 입력해 주세요.") @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 핸드폰 번호 형식이 아닙니다. (- 제외)") String phoneNumber) {
}