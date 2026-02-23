package com.kt.onrace.common.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kt.onrace.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
	private final boolean success;
	private final String code;
	private final String message;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final T data;

	private final LocalDateTime timestamp;

	private ApiResponse(boolean success, String code, String message, T data) {
		this.success = success;
		this.code = code;
		this.message = message;
		this.data = data;
		this.timestamp = LocalDateTime.now();
	}

	public static ApiResponse<Void> success() {
		return new ApiResponse<>(true, "SUCCESS", "요청이 성공했습니다", null);
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, "SUCCESS", null, data);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
		return new ApiResponse<>(false, errorCode.getCode(), message, null);
	}
}
