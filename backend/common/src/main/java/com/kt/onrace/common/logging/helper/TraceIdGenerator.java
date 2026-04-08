package com.kt.onrace.common.logging.helper;

import java.util.UUID;

import org.slf4j.MDC;

import jakarta.servlet.http.HttpServletRequest;

public class TraceIdGenerator {

	public static final String TRACE_ID_HEADER = "X-Trace-Id";
	public static final String TRACE_ID_MDC_KEY = "traceId";

	public static void getOrCreateTraceId(HttpServletRequest request) {
		String traceId = request.getHeader(TRACE_ID_HEADER);

		if (traceId == null || traceId.isEmpty()) {
			traceId = generateTraceId();
		}

		MDC.put(TRACE_ID_MDC_KEY, traceId);
	}

	public static String getCurrentTraceId() {
		return MDC.get(TRACE_ID_MDC_KEY);
	}

	private static String generateTraceId() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

	public static void clear() {
		MDC.remove(TRACE_ID_MDC_KEY);
	}
}
