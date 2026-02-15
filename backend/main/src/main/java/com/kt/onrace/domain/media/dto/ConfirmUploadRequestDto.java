package com.kt.onrace.domain.media.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmUploadRequestDto(
	@NotNull Long mediaId
) {
}
