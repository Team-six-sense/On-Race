package com.kt.onrace.domain.loadtest.dto;

import jakarta.validation.constraints.NotNull;

public record LoadTestConfirmRequest(
	@NotNull(message = "페이스 ID는 필수입니다")
	Long paceId
) {
}
