package com.kt.onrace.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	// [도메인]_[상태]_[대상]

	//COMMON
	COMMON_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	COMMON_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "유효하지 않은 요청 파라미터입니다"),
	COMMON_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 형식의 값입니다."),
	COMMON_SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "시스템 오류가 발생했습니다."),
	COMMON_HTTP_METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "지원되지 않는 HTTP 메서드입니다."),

	//AUTH
	AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
	AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 인증 토큰입니다."),
	AUTH_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 인증 토큰입니다."),
	AUTH_MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 형식의 인증 토큰입니다."),
	AUTH_CLAIM_ERROR_TOKEN(HttpStatus.UNAUTHORIZED, "인증 토큰 Claim 정보가 잘못되었습니다."),
	AUTH_ACCESS_BLACKLISTED(HttpStatus.UNAUTHORIZED, "로그아웃 된 토큰입니다. 다시 로그인 해주세요."),
	AUTH_NOT_FOUND_USER(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."),
	AUTH_INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
	AUTH_EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
	AUTH_MALFORMED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 형식의 JWT 토큰입니다."),


	;

	private final HttpStatus status;
	private final String message;

}

