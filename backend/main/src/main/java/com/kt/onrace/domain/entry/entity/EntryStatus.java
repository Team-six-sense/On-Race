package com.kt.onrace.domain.entry.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntryStatus {
	PRE_SAVED("사전정보저장"),
	APPLIED("신청완료"),
	QUEUED("대기중"),
	CONFIRMED("확정"),
	CANCELLED("취소");

	private final String description;
}
