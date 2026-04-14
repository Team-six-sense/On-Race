package com.kt.onrace.domain.order.contract;

import lombok.Builder;

/**
 * 주문/결제팀이 entry 도메인으로부터 받아야 하는 최소 결제 가능 계약.
 * entry는 결제 가능 여부와 실패 코드를 판정해 반환하고, order는 그 결과만 소비한다.
 */
@Builder
public record OrderCheckoutEligibility(
	Long entryId,
	boolean canCheckout,
	String failureCode
) {
}
