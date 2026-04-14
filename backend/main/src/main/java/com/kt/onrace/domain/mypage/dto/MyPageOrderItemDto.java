package com.kt.onrace.domain.mypage.dto;

import java.time.LocalDateTime;

/**
 * 마이페이지 주문 목록에서 한 건의 주문을 표현하는 DTO이다.
 * 주문 번호, 상태, 이벤트 정보, 결제 금액과 주문 시각을 담는다.
 */
public record MyPageOrderItemDto(
	String orderNumber,
	Long eventId,
	String status,
	String thumbnailUrl,
	String title,
	String courseName,
	String paceName,
	Long finalAmount,
	LocalDateTime orderedAt,
	LocalDateTime paymentDeadlineAt
) {
	public static MyPageOrderItemDto of(
		String orderNumber,
		Long eventId,
		String status,
		String thumbnailUrl,
		String title,
		String courseName,
		String paceName,
		Long finalAmount,
		LocalDateTime orderedAt,
		LocalDateTime paymentDeadlineAt
	) {
		return new MyPageOrderItemDto(
			orderNumber,
			eventId,
			status,
			thumbnailUrl,
			title,
			courseName,
			paceName,
			finalAmount,
			orderedAt,
			paymentDeadlineAt
		);
	}
}
