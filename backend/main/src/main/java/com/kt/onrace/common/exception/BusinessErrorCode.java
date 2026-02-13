package com.kt.onrace.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BusinessErrorCode implements ErrorCode {
	// [도메인]_[상태]_[대상]

	//COMMON
	COMMON_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "CMN_001","잘못된 요청입니다."),
	COMMON_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "CMN_002","유효하지 않은 요청 파라미터입니다"),
	COMMON_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "CMN_003","잘못된 형식의 값입니다."),
	COMMON_SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CMN_004","시스템 오류가 발생했습니다."),
	COMMON_HTTP_METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "CMN_005","지원되지 않는 HTTP 메서드입니다."),

	//AUTH
	AUTH_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003","지원되지 않는 인증 토큰입니다."),
	AUTH_MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004","잘못된 형식의 인증 토큰입니다."),
	AUTH_CLAIM_ERROR_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005","인증 토큰 Claim 정보가 잘못되었습니다."),
	AUTH_ACCESS_BLACKLISTED(HttpStatus.UNAUTHORIZED, "AUTH_006","로그아웃 된 토큰입니다. 다시 로그인 해주세요."),
	AUTH_NOT_FOUND_USER(HttpStatus.UNAUTHORIZED, "AUTH_007","존재하지 않는 사용자입니다."),
	AUTH_INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_008","유효하지 않은 JWT 토큰입니다."),
	AUTH_EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_009","만료된 JWT 토큰입니다."),
	AUTH_MALFORMED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_010","잘못된 형식의 JWT 토큰입니다."),


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

