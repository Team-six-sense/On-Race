package com.kt.onrace.domain.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {

	// 공통
	READY("대기중"),
	IN_PROGRESS("진행중"),
	END("마감"),

	// 응모
	DRAW_PENDING("결과 대기"),
	DRAW_COMPLETED("결과 발표"),

	// 선착
	FIRST_COME_CLOSED("선착 신청 마감");

	private final String description;
}
