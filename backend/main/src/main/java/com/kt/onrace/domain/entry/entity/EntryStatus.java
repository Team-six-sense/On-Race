package com.kt.onrace.domain.entry.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntryStatus {
	PRE_SAVED("사전정보저장"),
	APPLIED("신청완료"), // 응모는 응모 신청 후 applied, 신청은 결제 이후 applied
	WON("당첨"), // 응모는 이 상태에서 결제 가능
	LOST("미당첨");

	private final String description;
}
