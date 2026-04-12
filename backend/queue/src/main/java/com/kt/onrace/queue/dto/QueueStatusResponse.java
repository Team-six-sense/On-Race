package com.kt.onrace.queue.dto;

import lombok.Builder;

@Builder
public record QueueStatusResponse(
	Long paceId,
	String status,
	Long position,
	String passToken,
	Long retryAfterMs // 인프라 요청사항 - 폴링 지터 적용
) {
	public static QueueStatusResponse waiting(Long paceId, Long position, Long retryAfterMs) {
		return QueueStatusResponse.builder()
			.paceId(paceId)
			.status("WAITING")
			.position(position)
			.retryAfterMs(retryAfterMs)
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
