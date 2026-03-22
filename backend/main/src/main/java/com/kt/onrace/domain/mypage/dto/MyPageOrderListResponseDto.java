package com.kt.onrace.domain.mypage.dto;

import java.util.List;

/**
 * 마이페이지 주문 목록 페이지 응답을 표현하는 DTO이다.
 * 페이지 정보, 다음 페이지 존재 여부, 주문 항목 목록을 함께 전달한다.
 */
public record MyPageOrderListResponseDto(
	int page,
	int size,
	long totalCount,
	boolean hasNext,
	List<MyPageOrderItemDto> items
) {
	public MyPageOrderListResponseDto {
		items = items == null ? List.of() : List.copyOf(items);
	}

	public static MyPageOrderListResponseDto of(int page, int size, long totalCount, List<MyPageOrderItemDto> items) {
		List<MyPageOrderItemDto> safeItems = items == null ? List.of() : List.copyOf(items);
		boolean hasNext = (long) (page + 1) * size < totalCount;
		return new MyPageOrderListResponseDto(page, size, totalCount, hasNext, safeItems);
	}

	public static MyPageOrderListResponseDto empty(int page, int size) {
		return new MyPageOrderListResponseDto(page, size, 0, false, List.of());
	}
}
