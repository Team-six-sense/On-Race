package com.kt.onrace.domain.mypage.dto;

/**
 * 마이페이지 화면에서 공통으로 사용하는 상태 표시 DTO이다.
 * 상태 문구와 함께 노출할 액션 종류, 라벨, 활성화 여부를 표현한다.
 */
public record MyPageStatusDto(
	String statusText,
	String actionType,
	String actionLabel,
	boolean actionEnabled
) {
	public static MyPageStatusDto of(
		String statusText,
		String actionType,
		String actionLabel,
		boolean actionEnabled
	) {
		return new MyPageStatusDto(statusText, actionType, actionLabel, actionEnabled);
	}
}
