package com.kt.onrace.domain.entry.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public record EntryPreSaveRequest(
	@NotNull
	Long courseId,

	@NotNull
	Long paceId

) {
}
