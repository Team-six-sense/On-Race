package com.kt.onrace.domain.event.dto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kt.onrace.domain.event.entity.EventStatus;

public record EventCursorData(
	int statusPriority,
	LocalDateTime eventAt,
	String title,
	Long id
) {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
		.registerModule(new JavaTimeModule());

	public static EventCursorData from(EventListResponse response) {
		return new EventCursorData(
			response.status().getSortOrder(),
			response.eventAt(),
			response.title(),
			response.id()
		);
	}

	/**
	 * 커서 데이터를 Base64 인코딩된 JSON 문자열로 변환
	 */
	public String encode() {
		try {
			String json = OBJECT_MAPPER.writeValueAsString(this);
			return Base64.getUrlEncoder().withoutPadding()
				.encodeToString(json.getBytes(StandardCharsets.UTF_8));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("커서 인코딩 실패", e);
		}
	}

	/**
	 * Base64 인코딩된 커서 문자열을 EventCursorData로 디코딩
	 */
	public static EventCursorData decode(String cursor) {
		try {
			byte[] decoded = Base64.getUrlDecoder().decode(cursor);
			String json = new String(decoded, StandardCharsets.UTF_8);
			return OBJECT_MAPPER.readValue(json, EventCursorData.class);
		} catch (Exception e) {
			throw new IllegalArgumentException("유효하지 않은 커서 값입니다", e);
		}
	}
}
