package com.kt.onrace.queue.dto;

import lombok.Builder;

@Builder
public record QueueStatusResponse(
	Long paceId,
	String status,
	Long position,
	String passToken
) {
	public static QueueStatusResponse waiting(Long paceId, Long position) {
		return QueueStatusResponse.builder()
			.paceId(paceId)
			.status("WAITING")
			.position(position)
			.build();
	}

	public static QueueStatusResponse pass(Long paceId, String passToken) {
		return QueueStatusResponse.builder()
			.paceId(paceId)
			.status("PASS")
			.position(null)
			.passToken(passToken)
			.build();
	}
}
