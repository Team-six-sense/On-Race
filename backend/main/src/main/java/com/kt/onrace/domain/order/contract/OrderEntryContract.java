package com.kt.onrace.domain.order.contract;

/**
 * PAY-00에서 고정한 주문팀 관점의 entry 연동 계약.
 * 실제 구현은 entry/stock 팀 로직에 맞춰 별도 adapter가 제공되어야 한다.
 */
public interface OrderEntryContract {

	OrderCheckoutEligibility resolveCheckoutEligibility(Long userId, Long eventId, Long paceId);

	boolean hasReservation(Long eventId, Long paceId, Long userId);

	void confirmReservation(Long userId, Long eventId, Long paceId);
}
