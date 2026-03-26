package com.kt.onrace.domain.mypage.dto;

import java.time.LocalDateTime;

/**
 * 마이페이지 신청내역 기본 목록 한 건을 표현하는 DTO이다.
 * 목록 행 렌더링에 필요한 참여일, 이벤트/옵션, 상태, 액션 정보를 담는다.
 */
public record MyPageApplicationHistoryItemDto(
	Long entryId,
	Long eventId,
	LocalDateTime participatedAt,
	String thumbnailUrl,
	String eventName,
	SelectedOption selectedOption,
	EventMethod eventMethod,
	RecruitmentSchedule recruitmentSchedule,
	String statusDisplayValue,
	Action action
) {
	public record SelectedOption(
		String courseName,
		String paceName,
		String displayValue
	) {
	}

	public record EventMethod(
		String code,
		String label
	) {
	}

	public record RecruitmentSchedule(
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
	}

	public record Action(
		String type,
		String label,
		boolean enabled
	) {
	}
}
