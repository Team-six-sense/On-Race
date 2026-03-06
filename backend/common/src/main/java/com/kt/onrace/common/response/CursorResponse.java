package com.kt.onrace.common.response;

import java.util.List;
import java.util.function.Function;

public record CursorResponse<T>(
	List<T> content,
	String nextCursor, // 커서 다중 정렬로 인해 변경
	boolean hasNext
) {

	public static <E, T> CursorResponse<T> of(List<E> entities, int fetchSize,
		Function<E, T> mapper, Function<T, Long> cursorExtractor) {

		boolean hasNext = entities.size() > fetchSize;

		List<T> content = entities.stream()
			.limit(fetchSize)
			.map(mapper)
			.toList();

		String nextCursor = hasNext
			? String.valueOf(cursorExtractor.apply(content.get(content.size() - 1)))
			: null;

		return new CursorResponse<>(content, nextCursor, hasNext);
	}

	/**
	 * 다중 정렬 키셋 커서 페이징용 팩토리 메서드
	 */
	public static <E, T> CursorResponse<T> ofKeyset(List<E> entities, int fetchSize,
		Function<E, T> mapper, Function<T, String> cursorExtractor) {

		boolean hasNext = entities.size() > fetchSize;

		List<T> content = entities.stream()
			.limit(fetchSize)
			.map(mapper)
			.toList();

		String nextCursor = hasNext
			? cursorExtractor.apply(content.get(content.size() - 1))
			: null;

		return new CursorResponse<>(content, nextCursor, hasNext);
	}
}
