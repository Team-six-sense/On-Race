package com.kt.onrace.domain.entry.dto;

import jakarta.validation.constraints.NotNull;

public record EntryCoursePaceRequest(
	@NotNull
	Long courseId,

	@NotNull
	Long paceId

) {
}
