package com.kt.onrace.domain.mypage.dto;

import java.time.LocalDateTime;

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
}
