package com.kt.onrace.domain.media.dto;

import lombok.Builder;

@Builder
public record ConfirmUploadResponseDto(
	Long mediaId,
	String status
) {
}
