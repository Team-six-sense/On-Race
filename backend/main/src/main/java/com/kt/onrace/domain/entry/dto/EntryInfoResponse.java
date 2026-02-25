package com.kt.onrace.domain.entry.dto;

import lombok.Builder;

@Builder
public record EntryInfoResponse(
	long entryCount,
	int capacity,
	boolean isCompetitive,
	double rate,
	long price
) {
}
