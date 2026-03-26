package com.kt.onrace.auth.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.auth.dto.TermResponse;
import com.kt.onrace.auth.service.TermService;
import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.swagger.SwaggerAssistance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Term", description = "약관 API")
@ApiLog
@RestController
@RequestMapping
@RequiredArgsConstructor
public class TermController extends SwaggerAssistance {

	private final TermService termService;

	@Operation(summary = "활성 약관 목록 조회", description = "회원가입 시 동의가 필요한 약관 목록 조회")
	@GetMapping("/terms")
	public ApiResponse<List<TermResponse>> getActiveTerms() {
		return ApiResponse.success(termService.getActiveTerms());
	}
}
