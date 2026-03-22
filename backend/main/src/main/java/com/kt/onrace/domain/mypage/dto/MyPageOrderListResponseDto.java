package com.kt.onrace.domain.mypage.dto;

import java.util.List;

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
