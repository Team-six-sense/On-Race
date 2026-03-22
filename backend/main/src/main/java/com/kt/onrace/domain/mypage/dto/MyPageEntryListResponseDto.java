package com.kt.onrace.domain.mypage.dto;

import java.util.List;

/**
 * 마이페이지 신청 이력 목록 페이지 응답을 표현하는 DTO이다.
 * 페이지 정보, 다음 페이지 존재 여부, 신청 항목 목록을 함께 전달한다.
 */
public record MyPageEntryListResponseDto(
	int page,
	int size,
	long totalCount,
	boolean hasNext,
	List<MyPageEntryItemDto> items
) {
	public MyPageEntryListResponseDto {
		items = items == null ? List.of() : List.copyOf(items);
	}

	public static MyPageEntryListResponseDto of(int page, int size, long totalCount, List<MyPageEntryItemDto> items) {
		List<MyPageEntryItemDto> safeItems = items == null ? List.of() : List.copyOf(items);
		boolean hasNext = (long) (page + 1) * size < totalCount;
		return new MyPageEntryListResponseDto(page, size, totalCount, hasNext, safeItems);
	}

	public static MyPageEntryListResponseDto empty(int page, int size) {
		return new MyPageEntryListResponseDto(page, size, 0, false, List.of());
	}
}
