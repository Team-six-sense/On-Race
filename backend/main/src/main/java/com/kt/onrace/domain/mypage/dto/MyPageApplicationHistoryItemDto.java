package com.kt.onrace.domain.mypage.dto;

import java.time.LocalDateTime;

import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventStatus;

/**
 * reduced scope 신청내역 화면에서 한 건의 카드를 표현하는 DTO이다.
 * 코스/페이스/가격은 화면 표시용 현재 조회값이며, 신청 시점 snapshot 의미를 보장하지 않는다.
 */
public record MyPageApplicationHistoryItemDto(
	Long entryId,
	Long eventId,
	String eventName,
	EventAppType applicationType,
	EventStatus eventStatus,
	String displayStatus,
	String actionType,
	String actionLabel,
	boolean actionEnabled,
	String deepLink,
	String thumbnailUrl,
	String courseName,
	String paceName,
	Long price,
	LocalDateTime appliedAt,
	LocalDateTime eventAt,
	LocalDateTime applicationStartAt,
	LocalDateTime applicationEndAt,
	LocalDateTime resultAnnouncedAt
) {
}
