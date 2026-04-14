package com.kt.onrace.domain.order.contract;

/**
 * 주문 도메인이 entry 도메인과 상호작용할 때 사용하는 계약.
 */
public interface OrderEntryContract {

	OrderCheckoutEligibility resolveCheckoutEligibility(Long userId, Long eventId, Long paceId);

	void handlePaymentConfirmed(Long entryId);

	void rollbackPendingPayment(Long entryId);
}
