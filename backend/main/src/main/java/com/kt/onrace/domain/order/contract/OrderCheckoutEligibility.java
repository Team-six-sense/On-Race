package com.kt.onrace.domain.order.contract;

import java.time.LocalDateTime;

import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.event.entity.EventAppType;

/**
 * 주문/결제팀이 entry 도메인으로부터 받아야 하는 최소 결제 가능 계약.
 * LOTTERY는 WON, FIRST_COME은 RESERVED + reservedUntil/예약 유효성 확인을 전제로 한다.
 */
public record OrderCheckoutEligibility(
	EventAppType appType,
	EntryStatus currentEntryStatus,
	EntryStatus requiredEntryStatus,
	LocalDateTime reservedUntil,
	boolean requiresReservationValidation,
	boolean canCheckout,
	String failureCode
) {
}
