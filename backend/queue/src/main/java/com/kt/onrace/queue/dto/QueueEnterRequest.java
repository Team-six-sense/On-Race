package com.kt.onrace.queue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QueueEnterRequest(
	@NotNull(message = "페이스 ID는 필수입니다.")
	@Min(value = 1, message = "페이스 ID는 1 이상이어야 합니다.")
	Long paceId
) {
}
