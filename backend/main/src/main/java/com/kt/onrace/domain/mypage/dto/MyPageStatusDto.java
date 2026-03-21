package com.kt.onrace.domain.mypage.dto;

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
