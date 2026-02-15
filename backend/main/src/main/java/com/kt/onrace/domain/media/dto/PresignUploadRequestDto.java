package com.kt.onrace.domain.media.dto;

import jakarta.validation.constraints.NotBlank;

public record PresignUploadRequestDto(@NotBlank String filename,
									  @NotBlank String contentType) {
}
