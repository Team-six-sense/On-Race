package com.kt.onrace.domain.entry.dto;

import lombok.Builder;

@Builder
public record EntryInfoResponse(
	long entryCount,
	int capacity,
	double competitionRate,
	double fillRatePercent,
	long price
) {
}
