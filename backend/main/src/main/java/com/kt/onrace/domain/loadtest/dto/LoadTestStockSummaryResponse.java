package com.kt.onrace.domain.loadtest.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record LoadTestStockSummaryResponse(
	long eventId,
	int dbTotalStock,
	int dbConfirmedStock,
	long redisRemainingStock,
	long redisConfirmedStock,
	int entryReservedCount,
	int entryAppliedCount,
	int entryPreSavedCount,
	boolean overselling,
	boolean dbRedisMatch,
	List<PaceDetail> paceDetails
) {

	@Builder
	public record PaceDetail(
		long paceId,
		String courseName,
		String paceName,
		int dbTotal,
		int dbConfirmed,
		long redisRemaining,
		long redisConfirmed,
		boolean match
	) {
	}
}
