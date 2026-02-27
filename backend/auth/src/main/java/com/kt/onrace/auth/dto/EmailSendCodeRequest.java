package com.kt.onrace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailSendCodeRequest(
	@NotBlank(message = "이메일을 입력해 주세요.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 100)
	String email
) {
}
