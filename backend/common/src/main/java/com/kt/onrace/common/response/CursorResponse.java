package com.kt.onrace.common.response;

import java.util.List;
import java.util.function.Function;

public record CursorResponse<T>(
	List<T> content,
	Long nextCursor,
	boolean hasNext
) {

	/**
	 * List<E> entities -> DB에서 가져온 엔티티 리스트
	 * 	int fetchSize -> 클라이언트가 요청한 페이지 사이즈
	 * 	Function<E, T> mapper -> 엔티티를 DTO로 변환하는 함수
	 * 	Function<T, Long> cursorExtractor -> DTO에서 다음 커서를 추출하는 함수
	 * 	E를 받아서 T로 변환한 후 T를 반환함
	 */
	public static <E, T> CursorResponse<T> of(List<E> entities, int fetchSize,
		Function<E, T> mapper, Function<T, Long> cursorExtractor) {

		boolean hasNext = entities.size() > fetchSize;

		List<T> content = entities.stream()
			.limit(fetchSize)
			.map(mapper)
			.toList();

		Long nextCursor = hasNext ? cursorExtractor.apply(content.get(content.size() - 1)) : null;

		return new CursorResponse<>(content, nextCursor, hasNext);
	}
}
