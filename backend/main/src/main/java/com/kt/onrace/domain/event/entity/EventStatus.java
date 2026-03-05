package com.kt.onrace.domain.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {

	// 공통
	READY("대기중"),
	IN_PROGRESS("신청중"),
	END("마감"),

	// 응모
	DRAW_COMPLETED("결과 발표");

	private final String description;
}
