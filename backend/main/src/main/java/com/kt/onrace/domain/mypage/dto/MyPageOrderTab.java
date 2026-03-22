package com.kt.onrace.domain.mypage.dto;

import java.util.Locale;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.order.entity.OrderStatus;

/**
 * 마이페이지 주문 목록의 탭 필터 값을 정의하는 enum이다.
 * 문자열 요청값을 안전하게 enum으로 변환하고 주문 상태와의 매칭 규칙을 제공한다.
 */
public enum MyPageOrderTab {
	ALL,
	PENDING,
	COMPLETED,
	CANCELLED;

	public static MyPageOrderTab from(String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return ALL;
		}

		try {
			return MyPageOrderTab.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new BusinessException(BusinessErrorCode.ORDER_INVALID_TAB);
		}
	}

	public boolean matches(OrderStatus orderStatus) {
		return switch (this) {
			case ALL -> true;
			case PENDING -> orderStatus == OrderStatus.PENDING;
			case COMPLETED -> orderStatus == OrderStatus.PAID;
			case CANCELLED -> orderStatus == OrderStatus.CANCELLED;
		};
	}
}
