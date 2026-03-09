package com.kt.onrace.domain.entry.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.kt.onrace.domain.entry.dto.EntryApplyResponse;
import com.kt.onrace.domain.entry.dto.EntryOverviewResponse;
import com.kt.onrace.domain.entry.dto.EntryCoursePaceRequest;
import com.kt.onrace.domain.entry.dto.EntryPreSaveResponse;
import com.kt.onrace.domain.entry.dto.EntryRateResponse;
import com.kt.onrace.domain.entry.service.EntryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@ApiLog
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/entries")
public class EntryController {

	private final EntryService entryService;

	@GetMapping("/overview")
	public ApiResponse<EntryOverviewResponse> getEntryOverview(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long eventId
	) {
		return ApiResponse.success(entryService.getEntryOverview(userId, eventId));
	}

	@PostMapping("/pre-save")
	public ApiResponse<EntryPreSaveResponse> savePreSave(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long eventId,
		@Valid @RequestBody EntryCoursePaceRequest request
	) {
		return ApiResponse.success(entryService.savePreSave(userId, eventId, request));
	}

	@GetMapping("/rate")
	public ApiResponse<EntryRateResponse> getEntryRate(
		@PathVariable Long eventId,
		@RequestParam Long courseId,
		@RequestParam Long paceId
	) {
		return ApiResponse.success(entryService.getEntryRate(eventId, courseId, paceId));
	}

	@DeleteMapping("/pre-save")
	public ApiResponse<Long> deletePreSave(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long eventId
	) {
		return ApiResponse.success(entryService.deletePreSave(userId, eventId));
	}

	@PostMapping("/apply")
	public ApiResponse<EntryApplyResponse> apply(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long eventId,
		@Valid @RequestBody EntryCoursePaceRequest request
	) {
		return ApiResponse.success(entryService.apply(userId, eventId, request));
	}
}
