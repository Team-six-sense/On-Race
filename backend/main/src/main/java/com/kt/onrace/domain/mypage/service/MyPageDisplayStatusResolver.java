package com.kt.onrace.domain.mypage.service;

import org.springframework.stereotype.Component;

import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderStatus;

/**
 * 주문 상태를 마이페이지 화면에 노출할 상태 문구와 액션 정보로 해석하는 컴포넌트이다.
 */
@Component
public class MyPageDisplayStatusResolver {

	private static final String ACTION_DETAIL = "DETAIL";

	public MyPageStatusDto resolveOrderStatus(Order currentOrder) {
		return switch (currentOrder.getOrderStatus()) {
			case PENDING -> MyPageStatusDto.of("결제 대기", ACTION_DETAIL, "주문 상세보기", true);
			case PAID -> MyPageStatusDto.of("결제 완료", ACTION_DETAIL, "주문 상세보기", true);
			case CANCELLED -> MyPageStatusDto.of("주문 취소", ACTION_DETAIL, "주문 상세보기", true);
			case EXPIRED -> MyPageStatusDto.of("주문 만료", ACTION_DETAIL, "주문 상세보기", true);
			case FAILED -> MyPageStatusDto.of("주문 실패", ACTION_DETAIL, "주문 상세보기", true);
		};
	}

	public boolean canCancel(OrderStatus orderStatus) {
		return orderStatus == OrderStatus.PENDING;
	}

	public boolean canRefund(OrderStatus orderStatus) {
		// MVP에서는 실제 금융 처리 대신 버튼 노출 가능 여부만 읽기 전용으로 제공한다.
		return orderStatus == OrderStatus.PAID;
	}

	public boolean canExchange(OrderStatus orderStatus) {
		return false;
	}
}
