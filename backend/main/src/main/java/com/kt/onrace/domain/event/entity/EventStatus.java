package com.kt.onrace.domain.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {

	// 공통
	READY("대기중", 1),
	IN_PROGRESS("신청중", 0),
	END("마감", 2),

	// 응모
	DRAW_COMPLETED("결과 발표", 3);

	private final String description;
	private final int sortOrder; // 기획에서 기본 정렬 지정
}
