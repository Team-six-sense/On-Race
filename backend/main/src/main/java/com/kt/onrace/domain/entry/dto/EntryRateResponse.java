package com.kt.onrace.domain.entry.dto;

public record EntryRateResponse(
	long entryCount,
	int capacity,
	long competitionRate,
	long fillRatePercent,
	long price
) {

	public static EntryRateResponse of(long totalCount, long appliedCount, int capacity, long price) {
		long competitionRate = 0;
		long fillRatePercent = 0;

		if (capacity > 0) {
			competitionRate = Math.round((double) totalCount / capacity * 100);
			fillRatePercent = Math.min(Math.round((double) appliedCount / capacity * 100), 100);
		}

		return new EntryRateResponse(totalCount, capacity, competitionRate, fillRatePercent, price);
	}
}
