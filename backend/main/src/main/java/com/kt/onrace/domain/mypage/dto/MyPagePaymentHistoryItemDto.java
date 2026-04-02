package com.kt.onrace.domain.mypage.dto;

import java.time.LocalDateTime;

import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventStatus;

/**
 * 마이페이지 결제내역 목록에서 한 건의 결제 이력을 표현하는 DTO이다.
 * 프론트 PaymentHistory 형식에 맞춘 flat 응답 DTO이다.
 */
public record MyPagePaymentHistoryItemDto(
	String id,
	Long eventId,
	String title,
	EventAppType appType,
	EventStatus status,
	String orderStatus,
	LocalDateTime date,
	LocalDateTime eventAt,
	String venue,
	String course,
	String pace,
	Long price
) {
	public static MyPagePaymentHistoryItemDto of(
		String id,
		Long eventId,
		String title,
		EventAppType appType,
		EventStatus status,
		String orderStatus,
		LocalDateTime date,
		LocalDateTime eventAt,
		String venue,
		String course,
		String pace,
		Long price
	) {
		return new MyPagePaymentHistoryItemDto(
			id,
			eventId,
			title,
			appType,
			status,
			orderStatus,
			date,
			eventAt,
			venue,
			course,
			pace,
			price
		);
	}
}
