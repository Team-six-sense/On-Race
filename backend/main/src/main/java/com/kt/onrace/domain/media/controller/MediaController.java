package com.kt.onrace.domain.media.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.auth.entity.User;
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
		@AuthenticationPrincipal User user,
		@Valid @RequestBody PresignUploadRequestDto request
	) {
		PresignUploadResponseDto response = mediaService.issueUploadUrl(user.getId(), request);

		return ApiResponse.success(response);
	}

	@PostMapping("/confirm")
	public ApiResponse<ConfirmUploadResponseDto> confirmUpload(
		@AuthenticationPrincipal User user,
		@Valid @RequestBody ConfirmUploadRequestDto request
	) {
		ConfirmUploadResponseDto response = mediaService.confirmUpload(user.getId(), request.mediaId());

		return ApiResponse.success(response);
	}

}
