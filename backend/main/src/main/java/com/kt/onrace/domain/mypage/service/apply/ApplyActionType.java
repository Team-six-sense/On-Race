package com.kt.onrace.domain.mypage.service.apply;

/**
 * 신청 화면에서 사용할 액션 종류와 기본 라벨을 정의한다.
 */
public enum ApplyActionType {
	NONE("NONE", null),
	EDIT("EDIT", "사전정보 수정"),
	APPLY("APPLY", "신청하기"),
	CHECKOUT("CHECKOUT", "결제하기");

	private final String code;
	private final String label;

	ApplyActionType(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String code() {
		return code;
	}

	public String label() {
		return label;
	}
}
