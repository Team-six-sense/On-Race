package com.kt.onrace.domain.event.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {

	// 공통
	CLOSING_SOON("마감임박", 0),
	IN_PROGRESS("신청중", 1),
	READY("대기중", 2),
	END("마감", 3),

	// 응모
	DRAW_COMPLETED("결과 발표", 4);

	public static EventStatus resolveStatus(EventAppType appType,
		LocalDateTime appStartAt, LocalDateTime appEndAt,
		LocalDateTime lotteryAnnouncedAt, boolean soldOut) {

		LocalDateTime now = LocalDateTime.now();

		if (now.isBefore(appStartAt)) {
			return READY;
		}

		if (!now.isAfter(appEndAt)) {
			if(appType == EventAppType.FIRST_COME && soldOut)
				return END;

			if(!now.isBefore(appEndAt.minusDays(2)))
				return CLOSING_SOON;

			return IN_PROGRESS;
		}

		if (appType == EventAppType.LOTTERY && lotteryAnnouncedAt != null
			&& !now.isBefore(lotteryAnnouncedAt)) {
			return DRAW_COMPLETED;
		}

		return END;
	}

	private final String description;
	private final int sortOrder; // 기획에서 기본 정렬 지정
}
