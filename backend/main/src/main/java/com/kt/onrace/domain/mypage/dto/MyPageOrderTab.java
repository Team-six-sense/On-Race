package com.kt.onrace.domain.mypage.dto;

import java.util.Locale;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.order.entity.OrderStatus;

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
