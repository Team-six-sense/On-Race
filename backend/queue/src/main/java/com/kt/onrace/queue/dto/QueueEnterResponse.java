package com.kt.onrace.queue.dto;

import lombok.Builder;

@Builder
public record QueueEnterResponse(
	Long paceId,
	Long position
) {
	public static QueueEnterResponse of(Long paceId, Long position) {
		return QueueEnterResponse.builder()
			.paceId(paceId)
			.position(position)
			.build();
	}
}
