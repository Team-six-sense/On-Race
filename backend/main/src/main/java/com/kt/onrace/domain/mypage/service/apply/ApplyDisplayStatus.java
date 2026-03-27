package com.kt.onrace.domain.mypage.service.apply;

/**
 * 신청내역 화면에 노출할 내부 상태를 정의한다.
 */
public enum ApplyDisplayStatus {
	PRE_ENTRY_SAVED("사전정보 저장"),
	WAITING_TO_APPLY("신청 대기"),
	AVAILABLE_TO_APPLY("신청 가능"),
	ENTRY_CLOSED("신청 마감"),
	ENTRY_UNAVAILABLE("신청 불가"),
	RESERVED("예약 중"),
	LOTTERY_APPLIED("응모 완료"),
	RESULT_PENDING("결과 발표 대기"),
	RESULT_CHECK_REQUIRED("결과 확인 필요"),
	ENTRY_APPLIED("신청 완료"),
	WON("당첨"),
	LOST("미당첨");

	private final String label;

	ApplyDisplayStatus(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}
