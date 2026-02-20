package com.kt.onrace.common.exception;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BusinessErrorCode implements ErrorCode {
	// [도메인]_[상태]_[대상]

	//COMMON
	COMMON_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "CMN_001", "잘못된 요청입니다."),
	COMMON_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "CMN_002", "유효하지 않은 요청 파라미터입니다"),
	COMMON_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "CMN_003", "잘못된 형식의 값입니다."),
	COMMON_SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CMN_004", "시스템 오류가 발생했습니다."),
	COMMON_HTTP_METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "CMN_005", "지원되지 않는 HTTP 메서드입니다."),

	//AUTH
	AUTH_INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 JWT 토큰입니다."),
	AUTH_EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "만료된 JWT 토큰입니다."),
	AUTH_MALFORMED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "잘못된 형식의 JWT 토큰입니다."),
	AUTH_BLACKLISTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "로그아웃 된 토큰입니다. 다시 로그인 해주세요."),
	AUTH_NOT_FOUND_USER(HttpStatus.UNAUTHORIZED, "AUTH_005", "존재하지 않는 사용자입니다."),
	AUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_006", "인증에 실패했습니다."),
	AUTH_FORBIDDEN_USER(HttpStatus.FORBIDDEN, "AUTH_007", "접근 권한이 없습니다."),

	//MEDIA
	MEDIA_UNSUPPORTED_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "MDA_001", "허용되지 않은 Content_type 입니다."),
	MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "MDA_002", "미디어 정보를 찾을 수 없습니다."),
	MEDIA_OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "MDA_003", "S3에서 업로드된 파일을 찾을 수 없습니다."),
	MEDIA_CONFIRM_FAILED(HttpStatus.BAD_REQUEST, "MDA_004", "업로드 확정에 실패했습니다."),

	//EVENT
	EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVT_001", "이벤트를 찾을 수 없습니다.")

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

