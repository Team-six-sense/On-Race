package com.kt.onrace.domain.mypage.dto;

import java.util.List;

/**
 * 마이페이지 신청내역 기본 목록 응답 DTO이다.
 * 필터별 개수, 페이지 정보, 빈 상태 정보와 목록 아이템을 함께 전달한다.
 */
public record MyPageApplicationHistoryListResponseDto(
	String filter,
	Counts counts,
	int page,
	int size,
	long totalCount,
	boolean hasNext,
	EmptyState emptyState,
	List<MyPageApplicationHistoryItemDto> items
) {
	public MyPageApplicationHistoryListResponseDto {
		items = items == null ? List.of() : List.copyOf(items);
		counts = counts == null ? new Counts(0, 0, 0) : counts;
		emptyState = emptyState == null ? EmptyState.notEmpty() : emptyState;
	}

	public static MyPageApplicationHistoryListResponseDto of(
		String filter,
		Counts counts,
		int page,
		int size,
		long totalCount,
		List<MyPageApplicationHistoryItemDto> items
	) {
		List<MyPageApplicationHistoryItemDto> safeItems = items == null ? List.of() : List.copyOf(items);
		boolean hasNext = (long) (page + 1) * size < totalCount;
		return new MyPageApplicationHistoryListResponseDto(
			filter,
			counts,
			page,
			size,
			totalCount,
			hasNext,
			EmptyState.notEmpty(),
			safeItems
		);
	}

	public static MyPageApplicationHistoryListResponseDto empty(
		String filter,
		Counts counts,
		int page,
		int size,
		String title,
		String description
	) {
		return new MyPageApplicationHistoryListResponseDto(
			filter,
			counts,
			page,
			size,
			0,
			false,
			new EmptyState(true, title, description),
			List.of()
		);
	}

	public record Counts(
		long all,
		long lottery,
		long firstCome
	) {
	}

	public record EmptyState(
		boolean empty,
		String title,
		String description
	) {
		private static EmptyState notEmpty() {
			return new EmptyState(false, null, null);
		}
	}
}
