package com.kt.onrace.domain.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {

	WAITING_ENTRY("응모대기"),
	ENTRY_OPEN("응모중"),
	WAITING_RESULT("결과대기"),
	RESULT_ANNOUNCED("결과발표"),
	STANDBY("대기중"),
	APPLICATION_OPEN("신청중"),
	APPLICATION_CLOSED("신청마감");

	private final String description;
}
