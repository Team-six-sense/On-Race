package com.kt.onrace.domain.mypage.service.apply;

import java.util.Objects;

/**
 * 신청 화면 표시 정책 계산 결과이다.
 */
public record ApplyDisplayDecision(
	ApplyDisplayStatus displayStatus,
	ApplyActionType actionType,
	boolean actionEnabled,
	String helperText
) {
	public ApplyDisplayDecision {
		Objects.requireNonNull(displayStatus, "displayStatus must not be null");
		Objects.requireNonNull(actionType, "actionType must not be null");
	}

	public static ApplyDisplayDecision of(ApplyDisplayStatus displayStatus, ApplyActionType actionType) {
		return new ApplyDisplayDecision(displayStatus, actionType, actionType != ApplyActionType.NONE, null);
	}
}
