package com.kt.onrace.domain.main.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.logging.annotation.ApiLog;
import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.common.swagger.SwaggerAssistance;
import com.kt.onrace.domain.main.domain.Main;
import com.kt.onrace.domain.main.dto.TestRequest;
import com.kt.onrace.domain.main.service.MainService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@ApiLog
@RequiredArgsConstructor
@Tag(name = "Main", description = "메인 API")
@RestController
@RequestMapping("/api/main")
public class MainController extends SwaggerAssistance {

	private final MainService mainService;

	@Operation(summary = "동기 요청 테스트 (개발용)")
	@GetMapping("/sync")
	public ApiResponse<List<Main>> sync(
		@Parameter(description = "테스트 메시지", example = "Hello")
		@RequestParam String message,
		@RequestBody TestRequest request
	) {
		return ApiResponse.success(mainService.getSyncTestMessage(message, request, 111));
	}

	@Operation(summary = "비동기 요청 테스트 (개발용)")
	@GetMapping("/async")
	public ApiResponse<Void> async(
		@Parameter(description = "테스트 메시지", example = "Hello")
		@RequestParam String message,
		@RequestBody TestRequest request
	) {
		mainService.getAsyncTestMessage(message, request, 111);
		return ApiResponse.success();
	}
}
