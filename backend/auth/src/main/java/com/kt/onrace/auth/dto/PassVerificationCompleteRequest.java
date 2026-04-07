package com.kt.onrace.auth.dto;

import java.time.LocalDate;

import com.kt.onrace.auth.entity.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PassVerificationCompleteRequest(
	@NotBlank(message = "이름을 입력해 주세요.") @Size(max = 50, message = "이름은 50자 이하이어야 합니다.") String name,

	@NotNull(message = "성별을 입력해 주세요.") Gender gender,

	@NotNull(message = "생년월일을 입력해 주세요.") LocalDate birthdate
) {
}
