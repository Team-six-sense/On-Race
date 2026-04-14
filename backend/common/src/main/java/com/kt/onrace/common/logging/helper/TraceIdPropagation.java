package com.kt.onrace.common.logging.helper;

import java.util.function.BiConsumer;

import org.springframework.http.client.ClientHttpRequestInterceptor;

public final class TraceIdPropagation {

	private TraceIdPropagation() {
	}

	// MDC에서 traceId를 읽어 헤더로 전파 — Servlet 기반 HTTP 클라이언트용
	public static void setTraceHeader(BiConsumer<String, String> headerSetter) {
		String traceId = TraceIdGenerator.getCurrentTraceId();
		if (traceId != null && !traceId.isEmpty()) {
			headerSetter.accept(TraceIdGenerator.TRACE_ID_HEADER, traceId);
		}
	}

	// MDC에서 traceId를 읽어 X-Trace-Id 헤더로 전파하는 RestClient 인터셉터
	public static ClientHttpRequestInterceptor restClientInterceptor() {
		return (request, body, execution) -> {
			String traceId = TraceIdGenerator.getCurrentTraceId();
			if (traceId != null && !traceId.isEmpty()) {
				request.getHeaders().set(TraceIdGenerator.TRACE_ID_HEADER, traceId);
			}
			return execution.execute(request, body);
		};
	}
}
