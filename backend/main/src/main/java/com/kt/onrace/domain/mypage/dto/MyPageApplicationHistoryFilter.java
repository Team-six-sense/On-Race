package com.kt.onrace.domain.mypage.dto;

import java.util.Locale;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.event.entity.EventAppType;

/**
 * 신청내역 화면에서 사용하는 서버측 필터 값이다.
 * 화면은 단일 신청내역 목록을 사용하고, 필터는 전체/응모/선착순만 지원한다.
 */
public enum MyPageApplicationHistoryFilter {
	ALL,
	LOTTERY,
	FIRST_COME;

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

	public boolean matches(EventAppType appType) {
		return switch (this) {
			case ALL -> true;
			case LOTTERY -> appType == EventAppType.LOTTERY;
			case FIRST_COME -> appType == EventAppType.FIRST_COME;
		};
	}
}
