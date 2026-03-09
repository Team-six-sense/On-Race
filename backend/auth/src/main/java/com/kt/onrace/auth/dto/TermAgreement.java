package com.kt.onrace.auth.dto;

import jakarta.validation.constraints.NotNull;

public record TermAgreement(
		@NotNull(message = "약관 버전 ID를 입력해 주세요.") Long termVersionId,
		boolean agreed) {
}
