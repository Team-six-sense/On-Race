package com.kt.onrace.common.exception;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BusinessErrorCode implements ErrorCode {
	// [도메인]_[상태]_[대상]

	// COMMON
	COMMON_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "CMN_001", "잘못된 요청입니다."),
	COMMON_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "CMN_002", "유효하지 않은 요청 파라미터입니다"),
	COMMON_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "CMN_003", "잘못된 형식의 값입니다."),
	COMMON_SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CMN_004", "시스템 오류가 발생했습니다."),
	COMMON_HTTP_METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "CMN_005", "지원되지 않는 HTTP 메서드입니다."),
	COMMON_DUPLICATE_REQUEST(HttpStatus.CONFLICT, "CMN_006", "이미 처리된 요청입니다."),

	// AUTH
	AUTH_INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 JWT 토큰입니다."),
	AUTH_EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "만료된 JWT 토큰입니다."),
	AUTH_MALFORMED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "잘못된 형식의 JWT 토큰입니다."),
	AUTH_BLACKLISTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "로그아웃 된 토큰입니다. 다시 로그인 해주세요."),
	AUTH_NOT_FOUND_USER(HttpStatus.UNAUTHORIZED, "AUTH_005", "존재하지 않는 사용자입니다."),
	AUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_006", "인증에 실패했습니다."),
	AUTH_FORBIDDEN_USER(HttpStatus.FORBIDDEN, "AUTH_007", "접근 권한이 없습니다."),
	AUTH_DUPLICATE_LOGIN_ID(HttpStatus.BAD_REQUEST, "AUTH_008", "이미 사용 중인 로그인 ID입니다."),
	AUTH_DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "AUTH_009", "이미 사용 중인 이메일입니다."),
	AUTH_DUPLICATE_PHONE(HttpStatus.BAD_REQUEST, "AUTH_018", "이미 가입된 휴대폰 번호입니다."),
	AUTH_TOO_MANY_VERIFY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH_019", "인증 시도 횟수를 초과해 차단되었습니다. 인증 번호를 다시 요청해주세요."),
	AUTH_TOO_MANY_SEND_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH_020", "인증 번호 전송 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),
	AUTH_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_010", "비밀번호가 일치하지 않습니다."),
	AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_011", "유효하지 않은 리프레시 토큰입니다."),
	AUTH_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "AUTH_012", "이미 탈퇴한 계정입니다."),
	AUTH_DUPLICATE_USER_ID(HttpStatus.BAD_REQUEST, "AUTH_013", "이미 해당 계정은 생성되었습니다."),
	AUTH_INVALID_EMAIL_CODE(HttpStatus.BAD_REQUEST, "AUTH_014", "이메일 인증코드가 올바르지 않거나 만료되었습니다."),
	AUTH_EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_015", "이메일 인증이 완료되지 않았습니다."),
	AUTH_PASSWORD_RESET_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH_016", "가입되지 않은 이메일이거나 비밀번호 재설정이 불가한 계정입니다."),
	AUTH_PASSWORD_RESET_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "AUTH_017", "재발송은 1분 후에 가능합니다."),
	AUTH_PASSWORD_RESET_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_022", "비밀번호 재설정 요청 횟수를 초과했습니다. (5회/24시간)"),
	AUTH_INVALID_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_023", "유효하지 않거나 만료된 비밀번호 재설정 링크입니다."),
	AUTH_PASSWORD_RESET_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_024", "비밀번호 재설정 인증이 완료되지 않았습니다."),
	AUTH_SAME_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_021", "기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다."),
	AUTH_INVALID_PHONE_CODE(HttpStatus.BAD_REQUEST, "AUTH_025", "휴대폰 인증코드가 올바르지 않거나 만료되었습니다."),
	AUTH_PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_026", "휴대폰 인증이 완료되지 않았습니다."),
	AUTH_EMAIL_SEND_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "AUTH_027", "재발송은 1분 후에 가능합니다."),
	AUTH_EMAIL_SEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_028", "인증 이메일 발송 횟수를 초과했습니다. (5회/24시간)"),
	AUTH_LOGIN_FAIL_WARNING(HttpStatus.UNAUTHORIZED, "AUTH_029", "로그인 5회 연속 실패했습니다. 잠시 후 다시 시도해주세요."),
	AUTH_LOGIN_FAIL_CAPTCHA(HttpStatus.UNAUTHORIZED, "AUTH_030", "비정상적인 로그인 시도가 감지되었습니다. CAPTCHA를 완료해주세요."),
	AUTH_REQUIRED_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST, "AUTH_031", "필수 약관에 동의해 주세요."),
	AUTH_TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_032", "약관 정보를 찾을 수 없습니다."),

	// MEDIA
	MEDIA_UNSUPPORTED_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "MDA_001", "허용되지 않은 Content_type 입니다."),
	MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "MDA_002", "미디어 정보를 찾을 수 없습니다."),
	MEDIA_OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "MDA_003", "S3에서 업로드된 파일을 찾을 수 없습니다."),
	MEDIA_CONFIRM_FAILED(HttpStatus.BAD_REQUEST, "MDA_004", "업로드 확정에 실패했습니다."),

	// EVENT
	EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVT_001", "이벤트를 찾을 수 없습니다."),
	EVENT_NOT_IN_STANDBY(HttpStatus.BAD_REQUEST, "EVT_002", "사전정보 저장은 대기중 상태에서만 가능합니다."),
	EVENT_COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "EVT_003", "해당 이벤트의 코스를 찾을 수 없습니다."),
	EVENT_PACE_NOT_FOUND(HttpStatus.NOT_FOUND, "EVT_004", "해당 코스의 페이스를 찾을 수 없습니다."),
	EVENT_PRE_SAVE_NOT_FOUND(HttpStatus.NOT_FOUND, "EVT_005", "사전정보를 찾을 수 없습니다."),
	EVENT_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "EVT_006", "진행중인 이벤트가 아닙니다."),
	EVENT_ENDED(HttpStatus.BAD_REQUEST, "EVT_007", "종료된 이벤트입니다."),

	// ADDRESS
	ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADR_001", "배송지를 찾을 수 없습니다."),
	ADDRESS_DUPLICATE_LABEL(HttpStatus.CONFLICT, "ADR_002", "이미 사용 중인 배송지 별칭입니다."),
	ADDRESS_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "ADR_003", "배송지는 최대 10개까지 등록할 수 있습니다."),
	ADDRESS_INVALID_LABEL(HttpStatus.BAD_REQUEST, "ADR_004", "배송지 별칭은 20자 이하의 한글/영문/숫자/공백만 사용할 수 있습니다."),
	ADDRESS_DEFAULT_CONFLICT(HttpStatus.CONFLICT, "ADR_005", "기본 배송지 변경 중 충돌이 발생했습니다. 다시 시도해주세요."),
	ADDRESS_INVALID_PHONE(HttpStatus.BAD_REQUEST, "ADR_006", "전화번호는 숫자 10~11자리만 사용할 수 있습니다."),

	// ENTRY
	ENTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENT_001", "신청 정보를 찾을 수 없습니다."),
	ENTRY_COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "ENT_002", "해당 이벤트의 코스를 찾을 수 없습니다."),
	ENTRY_PACE_NOT_FOUND(HttpStatus.NOT_FOUND, "ENT_003", "해당 코스의 페이스를 찾을 수 없습니다."),
	ENTRY_EVENT_NOT_IN_STANDBY(HttpStatus.BAD_REQUEST, "ENT_004", "대기중 상태에서만 가능합니다."),
	ENTRY_NOT_IN_PERIOD(HttpStatus.BAD_REQUEST, "ENT_005", "신청 기간이 아닙니다."),
	ENTRY_ALREADY_APPLIED(HttpStatus.BAD_REQUEST, "ENT_006", "이미 신청한 이벤트입니다."),
	ENTRY_CANNOT_APPLY(HttpStatus.BAD_REQUEST, "ENT_007", "신청할 수 없는 상태입니다."),
	ENTRY_SOLD_OUT(HttpStatus.CONFLICT, "ENT_008", "신청이 마감되었습니다."),
	ENTRY_RESERVATION_EXPIRED(HttpStatus.BAD_REQUEST, "ENT_009", "만료되었습니다. 다시 신청해주세요."),
	ENTRY_ALREADY_RESERVED(HttpStatus.CONFLICT, "ENT_010", "이미 선점한 이벤트입니다."),
	ENTRY_EVENT_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "ENT_011", "종료된 이벤트입니다."),

	// STOCK
	STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STK_001", "재고 정보를 찾을 수 없습니다."),
	STOCK_NOT_INITIALIZED(HttpStatus.BAD_REQUEST, "STK_002", "재고가 초기화되지 않았습니다."),
	STOCK_CONFIRM_FAILED(HttpStatus.CONFLICT, "STK_003", "재고 확정에 실패했습니다."),

	// SALES_INFO
	SALES_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "SLS_001", "판매 정보를 찾을 수 없습니다."),

	// ORDER
	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORD_001", "주문 정보를 찾을 수 없습니다."),
	ORDER_INVALID_TAB(HttpStatus.BAD_REQUEST, "ORD_002", "유효하지 않은 주문 조회 탭입니다."),
	ORDER_CANNOT_CONFIRM(HttpStatus.BAD_REQUEST, "ORD_003", "현재 주문 상태에서는 결제 완료 처리할 수 없습니다."),

	// MEMBER
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MBR_001", "회원을 찾을 수 없습니다."),

	// QUEUE
	QUEUE_NOT_ENABLED(HttpStatus.BAD_REQUEST, "QUE_001", "해당 페이스의 대기열이 활성화되지 않았습니다."),
	QUEUE_ALREADY_ENTERED(HttpStatus.CONFLICT, "QUE_002", "이미 대기열에 진입한 사용자입니다."),
	QUEUE_NOT_FOUND(HttpStatus.NOT_FOUND, "QUE_003", "대기열에서 사용자를 찾을 수 없습니다.");

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
