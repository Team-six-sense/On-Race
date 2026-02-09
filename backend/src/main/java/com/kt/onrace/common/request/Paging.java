package com.kt.onrace.common.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import jakarta.validation.constraints.Min;

public record Paging(
	@Min(value = 1, message = "페이지는 1 이상이어야 합니다")
	int page,

	@Min(value = 1, message = "크기는 1 이상이어야 합니다")
	int size,

	String sortBy,
	String direction

) {
	public PageRequest toPageable() {
		if (sortBy != null && direction != null) {
			Sort.Direction dir = Sort.Direction.fromString(direction);
			return PageRequest.of(page - 1, size, Sort.by(dir, sortBy));
		}
		return PageRequest.of(page - 1, size);
	}
}
