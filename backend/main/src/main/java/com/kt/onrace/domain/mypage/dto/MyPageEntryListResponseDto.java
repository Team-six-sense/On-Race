package com.kt.onrace.domain.mypage.dto;

import java.util.List;

public record MyPageEntryListResponseDto(
	int totalCount,
	List<MyPageEntryItemDto> items
) {
	public MyPageEntryListResponseDto {
		items = items == null ? List.of() : List.copyOf(items);
	}

	public static MyPageEntryListResponseDto from(List<MyPageEntryItemDto> items) {
		List<MyPageEntryItemDto> safeItems = items == null ? List.of() : List.copyOf(items);
		return new MyPageEntryListResponseDto(safeItems.size(), safeItems);
	}

	public static MyPageEntryListResponseDto empty() {
		return new MyPageEntryListResponseDto(0, List.of());
	}
}
