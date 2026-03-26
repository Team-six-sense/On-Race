package com.kt.onrace.domain.mypage.dto;

import java.util.Locale;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.event.entity.EventAppType;

/**
 * 신청내역 기본 목록에서 사용하는 이벤트 방식 필터이다.
 * 전체, 추첨, 선착 탭과 빈 목록 문구를 함께 정의한다.
 */
public enum MyPageApplicationHistoryFilter {
	ALL(
		null,
		"신청 내역이 없어요.",
		"참여한 이벤트가 생기면 여기서 확인할 수 있어요."
	),
	LOTTERY(
		EventAppType.LOTTERY,
		"추첨 신청 내역이 없어요.",
		"추첨 방식 이벤트에 참여하면 여기서 확인할 수 있어요."
	),
	FIRST_COME(
		EventAppType.FIRST_COME,
		"선착 신청 내역이 없어요.",
		"선착 방식 이벤트에 참여하면 여기서 확인할 수 있어요."
	);

	private final EventAppType appType;
	private final String emptyTitle;
	private final String emptyDescription;

	MyPageApplicationHistoryFilter(
		EventAppType appType,
		String emptyTitle,
		String emptyDescription
	) {
		this.appType = appType;
		this.emptyTitle = emptyTitle;
		this.emptyDescription = emptyDescription;
	}

	public static MyPageApplicationHistoryFilter from(String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return ALL;
		}

		try {
			return MyPageApplicationHistoryFilter.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_PARAMETER);
		}
	}

	public EventAppType appType() {
		return appType;
	}

	public String emptyTitle() {
		return emptyTitle;
	}

	public String emptyDescription() {
		return emptyDescription;
	}

	public long pickCount(long allCount, long lotteryCount, long firstComeCount) {
		return switch (this) {
			case ALL -> allCount;
			case LOTTERY -> lotteryCount;
			case FIRST_COME -> firstComeCount;
		};
	}
}
