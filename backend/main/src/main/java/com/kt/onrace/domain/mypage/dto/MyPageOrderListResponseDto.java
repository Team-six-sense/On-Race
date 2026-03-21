package com.kt.onrace.domain.mypage.dto;

import java.util.List;

public record MyPageOrderListResponseDto(
	int totalCount,
	List<MyPageOrderItemDto> items
) {
	public MyPageOrderListResponseDto {
		items = items == null ? List.of() : List.copyOf(items);
	}

	public static MyPageOrderListResponseDto from(List<MyPageOrderItemDto> items) {
		List<MyPageOrderItemDto> safeItems = items == null ? List.of() : List.copyOf(items);
		return new MyPageOrderListResponseDto(safeItems.size(), safeItems);
	}

	public static MyPageOrderListResponseDto empty() {
		return new MyPageOrderListResponseDto(0, List.of());
	}
}
