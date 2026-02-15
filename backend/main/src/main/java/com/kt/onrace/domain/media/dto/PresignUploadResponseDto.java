package com.kt.onrace.domain.media.dto;

import lombok.Builder;

@Builder
public record PresignUploadResponseDto(Long mediaObjectId,
									   String objectKey,
									   String uploadUrl,
									   Long expiresIn) {
}
