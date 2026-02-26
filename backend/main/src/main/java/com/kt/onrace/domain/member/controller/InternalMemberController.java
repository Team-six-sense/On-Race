package com.kt.onrace.domain.member.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.member.service.MemberSyncService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/members")
public class InternalMemberController {

	private final MemberSyncService memberSyncService;

	@PostMapping("/{userId}/sync-create")
	public ApiResponse<Void> syncUserCreated(@PathVariable("userId") Long userId) {
		memberSyncService.syncUserCreated(userId);
		
		return ApiResponse.success(null);
	}

	@PostMapping("/{userId}/sync-delete")
	public ApiResponse<Void> syncUserDeleted(@PathVariable("userId") Long userId) {
		memberSyncService.syncUserDeleted(userId);

		return ApiResponse.success(null);
	}

}
