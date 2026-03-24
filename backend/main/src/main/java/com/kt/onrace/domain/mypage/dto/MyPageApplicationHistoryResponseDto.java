package com.kt.onrace.domain.mypage.dto;

import java.util.List;

/**
 * reduced scope 신청내역 화면 전용 응답 DTO이다.
 * 프론트가 화면 상태를 재조립하지 않도록 필터, empty 여부, 페이지 정보, 카드 목록을 함께 내려준다.
 */
public record MyPageApplicationHistoryResponseDto(
	MyPageApplicationHistoryFilter filter,
	boolean empty,
	Pagination pagination,
	List<MyPageApplicationHistoryItemDto> items
) {
	public MyPageApplicationHistoryResponseDto {
		items = items == null ? List.of() : List.copyOf(items);
	}

	public static MyPageApplicationHistoryResponseDto of(
		MyPageApplicationHistoryFilter filter,
		int page,
		int size,
		long totalCount,
		List<MyPageApplicationHistoryItemDto> items
	) {
		List<MyPageApplicationHistoryItemDto> safeItems = items == null ? List.of() : List.copyOf(items);
		return new MyPageApplicationHistoryResponseDto(
			filter,
			safeItems.isEmpty(),
			Pagination.of(page, size, totalCount),
			safeItems
		);
	}

	public static MyPageApplicationHistoryResponseDto empty(
		MyPageApplicationHistoryFilter filter,
		int page,
		int size
	) {
		return new MyPageApplicationHistoryResponseDto(
			filter,
			true,
			Pagination.of(page, size, 0),
			List.of()
		);
	}

	public record Pagination(
		int page,
		int size,
		long totalCount,
		boolean hasNext
	) {
		public static Pagination of(int page, int size, long totalCount) {
			boolean hasNext = (long) (page + 1) * size < totalCount;
			return new Pagination(page, size, totalCount, hasNext);
		}
	}
}
