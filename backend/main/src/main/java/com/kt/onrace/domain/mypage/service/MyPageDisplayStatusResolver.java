package com.kt.onrace.domain.mypage.service;

import org.springframework.stereotype.Component;

import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderStatus;

/**
 * 신청 및 주문 상태를 마이페이지 화면에 노출할 상태 문구와 액션 정보로 해석하는 컴포넌트이다.
 * 버튼 노출 여부와 라벨을 중앙에서 일관되게 결정한다.
 */
@Component
public class MyPageDisplayStatusResolver {

	private static final String ACTION_NONE = "NONE";
	private static final String ACTION_EDIT = "EDIT";
	private static final String ACTION_APPLY = "APPLY";
	private static final String ACTION_CHECKOUT = "CHECKOUT";
	private static final String ACTION_DETAIL = "DETAIL";

	public MyPageStatusDto resolveApplicationStatus(Event currentEvent, Entry currentEntry) {
		EntryStatus entryStatus = currentEntry.getStatus();
		EventStatus eventStatus = currentEvent.getStatus();

		if (entryStatus == EntryStatus.PRE_SAVED) {
			if (currentEvent.getAppType() == EventAppType.FIRST_COME) {
				if (eventStatus == EventStatus.IN_PROGRESS || eventStatus == EventStatus.CLOSING_SOON) {
					return MyPageStatusDto.of("신청 가능", ACTION_APPLY, "신청하기", true);
				}

				if (eventStatus == EventStatus.END) {
					return MyPageStatusDto.of("신청 불가", ACTION_NONE, null, false);
				}
			}

			return MyPageStatusDto.of("신청 대기", ACTION_EDIT, "사전정보 수정", true);
		}

		if (entryStatus == EntryStatus.RESERVED) {
			return MyPageStatusDto.of("예약 중", ACTION_CHECKOUT, "결제하기", true);
		}

		if (entryStatus == EntryStatus.APPLIED) {
			if (currentEvent.getAppType() == EventAppType.LOTTERY) {
				// Lottery는 결과 enum이 분리되지 않았기 때문에 APPLIED를 이벤트 phase와 함께 해석한다.
				if (eventStatus == EventStatus.IN_PROGRESS || eventStatus == EventStatus.CLOSING_SOON) {
					return MyPageStatusDto.of("응모 완료", ACTION_NONE, null, false);
				}

				if (eventStatus == EventStatus.END) {
					return MyPageStatusDto.of("결과 발표 대기", ACTION_NONE, null, false);
				}

				if (eventStatus == EventStatus.DRAW_COMPLETED) {
					return MyPageStatusDto.of("결과 확인 필요", ACTION_NONE, null, false);
				}
			}

			// First-come은 현재 APPLY_SUCCESS / APPLY_FAILED persisted state가 없어서
			// APPLIED를 "실신청 완료"로 간주하는 MVP 해석을 사용한다.
			return MyPageStatusDto.of("신청 완료", ACTION_NONE, null, false);
		}

		if (entryStatus == EntryStatus.WON) {
			return MyPageStatusDto.of("당첨", ACTION_CHECKOUT, "결제하기", true);
		}

		if (entryStatus == EntryStatus.LOST) {
			return MyPageStatusDto.of("미당첨", ACTION_NONE, null, false);
		}

		return MyPageStatusDto.of(entryStatus.getDescription(), ACTION_NONE, null, false);
	}

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
