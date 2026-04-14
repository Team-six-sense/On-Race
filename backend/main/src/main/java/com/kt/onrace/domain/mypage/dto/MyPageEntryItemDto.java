package com.kt.onrace.domain.mypage.dto;

import java.time.LocalDateTime;

/**
 * 마이페이지 신청 이력 목록에서 한 건의 신청 정보를 표현하는 DTO이다.
 * 상태, 이벤트/코스/페이스, 금액, 신청 시점과 결과 시점을 담는다.
 * resultAt 은 LOTTERY 의 결과 발표일이며 FIRST_COME 에서는 null 이다.
 */
public record MyPageEntryItemDto(
	Long entryId,
	Long eventId,
	String status,
	String thumbnailUrl,
	String title,
	String courseName,
	String paceName,
	Long price,
	LocalDateTime appliedAt,
	LocalDateTime resultAt
) {
	public static MyPageEntryItemDto of(
		Long entryId,
		Long eventId,
		String status,
		String thumbnailUrl,
		String title,
		String courseName,
		String paceName,
		Long price,
		LocalDateTime appliedAt,
		LocalDateTime resultAt
	) {
		return new MyPageEntryItemDto(
			entryId,
			eventId,
			status,
			thumbnailUrl,
			title,
			courseName,
			paceName,
			price,
			appliedAt,
			resultAt
		);
	}
}
