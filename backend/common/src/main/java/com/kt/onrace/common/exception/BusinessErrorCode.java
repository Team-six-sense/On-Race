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
	AUTH_DUPLICATE_LOGIN_ID(HttpStatus.BAD_REQUEST, "AUTH_008", "이미 사용 중인 로그인 ID입니다."),
	AUTH_DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "AUTH_009", "이미 사용 중인 이메일입니다."),
	AUTH_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_010", "비밀번호가 일치하지 않습니다."),
	AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_011", "유효하지 않은 리프레시 토큰입니다."),
	AUTH_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "AUTH_012", "이미 탈퇴한 계정입니다."),
	AUTH_DUPLICATE_USER_ID(HttpStatus.BAD_REQUEST, "AUTH_013", "이미 해당 계정은 생성되었습니다."),

	//MEDIA
	MEDIA_UNSUPPORTED_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "MDA_001", "허용되지 않은 Content_type 입니다."),
	MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "MDA_002", "미디어 정보를 찾을 수 없습니다."),
	MEDIA_OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "MDA_003", "S3에서 업로드된 파일을 찾을 수 없습니다."),
	MEDIA_CONFIRM_FAILED(HttpStatus.BAD_REQUEST, "MDA_004", "업로드 확정에 실패했습니다."),

	//EVENT
	EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVT_001", "이벤트를 찾을 수 없습니다."),
	EVENT_NOT_IN_STANDBY(HttpStatus.BAD_REQUEST, "EVT_002", "사전정보 저장은 대기중 상태에서만 가능합니다."),
	EVENT_COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "EVT_003", "해당 이벤트의 코스를 찾을 수 없습니다."),
	EVENT_PACE_NOT_FOUND(HttpStatus.NOT_FOUND, "EVT_004", "해당 코스의 페이스를 찾을 수 없습니다."),
	EVENT_PRE_SAVE_NOT_FOUND(HttpStatus.NOT_FOUND, "EVT_005", "사전정보를 찾을 수 없습니다."),

	//ENTRY
	ENTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENT_001", "신청 정보를 찾을 수 없습니다."),
	ENTRY_COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "ENT_002", "해당 이벤트의 코스를 찾을 수 없습니다."),
	ENTRY_PACE_NOT_FOUND(HttpStatus.NOT_FOUND, "ENT_003", "해당 코스의 페이스를 찾을 수 없습니다."),
	ENTRY_EVENT_NOT_IN_STANDBY(HttpStatus.BAD_REQUEST, "ENT_004", "대기중 상태에서만 가능합니다."),

	//STOCK
	STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STK_001", "재고 정보를 찾을 수 없습니다.");

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

