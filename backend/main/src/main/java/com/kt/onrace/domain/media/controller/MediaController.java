package com.kt.onrace.domain.media.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.media.dto.ConfirmUploadRequestDto;
import com.kt.onrace.domain.media.dto.ConfirmUploadResponseDto;
import com.kt.onrace.domain.media.dto.PresignUploadRequestDto;
import com.kt.onrace.domain.media.dto.PresignUploadResponseDto;
import com.kt.onrace.domain.media.service.MediaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
public class MediaController {

	private final MediaService mediaService;

	@PostMapping("/presign-upload")
	public ApiResponse<PresignUploadResponseDto> presignUpload(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody PresignUploadRequestDto request
	) {
		PresignUploadResponseDto response = mediaService.issueUploadUrl(userId, request);

		return ApiResponse.success(response);
	}

	@PostMapping("/confirm")
	public ApiResponse<ConfirmUploadResponseDto> confirmUpload(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody ConfirmUploadRequestDto request
	) {
		ConfirmUploadResponseDto response = mediaService.confirmUpload(userId, request.mediaId());

		return ApiResponse.success(response);
	}

}
