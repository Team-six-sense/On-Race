package com.kt.onrace.domain.entry.dto;

import java.time.LocalDateTime;

import com.kt.onrace.domain.entry.entity.Entry;

public record EntryApplyResponse(
	Long entryId,
	Long eventId,
	String status,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static EntryApplyResponse from(Entry entry) {
		return new EntryApplyResponse(
			entry.getId(),
			entry.getEvent().getId(),
			entry.getStatus().getDescription(),
			entry.getCreatedAt(),
			entry.getUpdatedAt()
		);
	}
}
