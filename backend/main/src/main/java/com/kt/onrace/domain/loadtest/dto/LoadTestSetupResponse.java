package com.kt.onrace.domain.loadtest.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record LoadTestSetupResponse(
	int userCount,
	boolean userSkipped,
	int totalStock,
	boolean redisInitialized,
	Map<String, Long> eventIds,
	Map<String, PaceMapEntry> paceMap,
	int preSaveCount
) {

	@Builder
	public record PaceMapEntry(
		List<PaceRef> hot,
		List<PaceRef> others
	) {
	}

	@Builder
	public record PaceRef(
		long courseId,
		long paceId,
		int stock
	) {
	}
}
