package com.kt.onrace.domain.mypage.service.apply;

import java.util.Objects;

import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventStatus;

/**
 * 신청 화면 표시 정책 계산에 필요한 입력값이다.
 */
public record ApplyDisplayStatusContext(
	ApplyDisplaySurface surface,
	EventAppType appType,
	EventStatus eventStatus,
	ApplyUserStatus userStatus,
	ApplyResultStatus resultStatus
) {
	public ApplyDisplayStatusContext {
		Objects.requireNonNull(surface, "surface must not be null");
		Objects.requireNonNull(appType, "appType must not be null");
		Objects.requireNonNull(eventStatus, "eventStatus must not be null");
		Objects.requireNonNull(userStatus, "userStatus must not be null");
		Objects.requireNonNull(resultStatus, "resultStatus must not be null");
	}
}
