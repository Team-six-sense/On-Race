package com.kt.onrace.domain.order.contract;

/**
 * PAY-00에서 고정한 주문팀 관점의 entry 연동 계약.
 * 실제 구현은 entry/stock 팀 로직에 맞춰 별도 adapter가 제공되어야 한다.
 */
public interface OrderEntryContract {

	OrderCheckoutEligibility resolveCheckoutEligibility(Long userId, Long eventId, Long paceId);

	boolean hasReservation(Long paceId, Long userId);

	void confirmReservation(Long userId, Long paceId);

	default void handlePaymentConfirmed(Long entryId) {
		// order 티켓 단계에서는 계약만 열어두고 실제 구현은 후속 작업에서 연결한다.
	}

	default void rollbackPendingPayment(Long entryId) {
		// order 티켓 단계에서는 계약만 열어두고 실제 구현은 후속 작업에서 연결한다.
	}
}
