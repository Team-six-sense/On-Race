package com.kt.onrace.domain.entry.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.entry.dto.EntryPreSaveRequest;
import com.kt.onrace.domain.entry.dto.EntryPreSaveResponse;
import com.kt.onrace.domain.entry.dto.EntryInfoResponse;
import com.kt.onrace.domain.entry.service.EntryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@ApiLog
@RestController
@RequiredArgsConstructor
@RequestMapping("/entries")
public class EntryController {

	private final EntryService entryService;

	@PostMapping("/events/{eventId}/pre-save")
	public ApiResponse<EntryPreSaveResponse> savePreSave(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long eventId,
		@Valid @RequestBody EntryPreSaveRequest request
	) {
		return ApiResponse.success(entryService.savePreSave(userId, eventId, request));
	}

	@GetMapping("/events/{eventId}/participation-info")
	public ApiResponse<EntryInfoResponse> getParticipationInfo(
		@PathVariable Long eventId,
		@RequestParam Long courseId,
		@RequestParam Long paceId
	) {
		return ApiResponse.success(entryService.getParticipationInfo(eventId, courseId, paceId));
	}
}
