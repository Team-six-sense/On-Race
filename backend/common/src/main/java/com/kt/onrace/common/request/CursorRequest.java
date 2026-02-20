package com.kt.onrace.common.request;

public record CursorRequest(
	Long cursorId,
	Integer size
) {
	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 20;

	public int getValidSize() {
		if (size == null || size < 1) {
			return DEFAULT_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}
}
