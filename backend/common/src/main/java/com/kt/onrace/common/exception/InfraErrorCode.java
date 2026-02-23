package com.kt.onrace.common.exception;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum InfraErrorCode implements ErrorCode {
	// [도메인]_[상태]_[대상]

	//DB
	DB_ERROR_ACCESS(HttpStatus.INTERNAL_SERVER_ERROR, "DB_001", "데이터베이스 접근 중 오류가 발생했습니다."),
	DB_TIMEOUT_QUERY(HttpStatus.SERVICE_UNAVAILABLE, "DB_002", "데이터베이스 쿼리 시간이 초과되었습니다"),

	;

	private final HttpStatus status;
	private final String code;
	private final String message;

	@Override
	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
