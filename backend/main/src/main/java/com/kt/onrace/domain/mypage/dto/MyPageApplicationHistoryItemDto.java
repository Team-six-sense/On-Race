package com.kt.onrace.domain.mypage.dto;

import java.time.LocalDateTime;

import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventStatus;

/**
 * 마이페이지 신청내역 기본 목록 한 건을 표현하는 DTO이다.
 * 프론트 EventHistory 형식에 맞춘 flat 응답 DTO이다.
 * resultAt 은 LOTTERY 의 결과 발표일이며 FIRST_COME 에서는 null 이다.
 */
public record MyPageApplicationHistoryItemDto(
	Long id,
	Long eventId,
	String title,
	EventAppType appType,
	EventStatus status,
	String entryStatus,
	LocalDateTime date,
	LocalDateTime eventAt,
	LocalDateTime appStartAt,
	LocalDateTime appEndAt,
	LocalDateTime resultAt,
	String venue,
	String course,
	String pace
) {
	public static MyPageApplicationHistoryItemDto of(
		Long id,
		Long eventId,
		String title,
		EventAppType appType,
		EventStatus status,
		String entryStatus,
		LocalDateTime date,
		LocalDateTime eventAt,
		LocalDateTime appStartAt,
		LocalDateTime appEndAt,
		LocalDateTime resultAt,
		String venue,
		String course,
		String pace
	) {
		return new MyPageApplicationHistoryItemDto(
			id,
			eventId,
			title,
			appType,
			status,
			entryStatus,
			date,
			eventAt,
			appStartAt,
			appEndAt,
			resultAt,
			venue,
			course,
			pace
		);
	}
}
