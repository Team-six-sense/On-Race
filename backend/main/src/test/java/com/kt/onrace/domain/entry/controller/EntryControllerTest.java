package com.kt.onrace.domain.entry.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.onrace.common.exception.GlobalExceptionHandler;
import com.kt.onrace.common.filter.GatewayAccessFilter;
import com.kt.onrace.domain.entry.dto.EntryApplyResponse;
import com.kt.onrace.domain.entry.dto.EntryCoursePaceRequest;
import com.kt.onrace.domain.entry.dto.EntryStockCheckResponse;
import com.kt.onrace.domain.entry.service.EntryService;
import com.kt.onrace.domain.event.entity.EventAppType;

@WebMvcTest(
	controllers = EntryController.class,
	excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GatewayAccessFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class EntryControllerTest {

	private static final String USER_ID_HEADER = "X-User-Id";
	private static final Long USER_ID = 1L;
	private static final Long EVENT_ID = 7L;
	private static final Long COURSE_ID = 15L;
	private static final Long PACE_ID = 26L;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private EntryService entryService;

	@Test
	@DisplayName("응모 신청 성공 시 200과 엔트리 응답을 반환한다")
	void applyLottery_success() throws Exception {
		EntryApplyResponse response = new EntryApplyResponse(1L, EVENT_ID, "신청완료", null, null, null);
		given(entryService.apply(eq(USER_ID), eq(EVENT_ID), any(EntryCoursePaceRequest.class), isNull(), eq(EventAppType.LOTTERY)))
			.willReturn(response);

		mockMvc.perform(post("/events/{eventId}/entries/apply/lottery", EVENT_ID)
				.header(USER_ID_HEADER, USER_ID)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new EntryCoursePaceRequest(COURSE_ID, PACE_ID))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.entryId").value(1L))
			.andExpect(jsonPath("$.data.status").value("신청완료"));

		verify(entryService).apply(eq(USER_ID), eq(EVENT_ID), any(EntryCoursePaceRequest.class), isNull(), eq(EventAppType.LOTTERY));
	}

	@Test
	@DisplayName("선착순 신청 성공 시 200과 예약 응답을 반환한다")
	void applyFirstCome_success() throws Exception {
		LocalDateTime reservedUntil = LocalDateTime.of(2026, 4, 1, 12, 10, 0);
		EntryApplyResponse response = new EntryApplyResponse(1L, EVENT_ID, "선점완료", reservedUntil, null, null);
		given(entryService.apply(eq(USER_ID), eq(EVENT_ID), any(EntryCoursePaceRequest.class), isNull(), eq(EventAppType.FIRST_COME)))
			.willReturn(response);

		mockMvc.perform(post("/events/{eventId}/entries/apply/first-come", EVENT_ID)
				.header(USER_ID_HEADER, USER_ID)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new EntryCoursePaceRequest(COURSE_ID, PACE_ID))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.status").value("선점완료"))
			.andExpect(jsonPath("$.data.reservedUntil").isNotEmpty());
	}

	@Test
	@DisplayName("선착순 신청 시 X-Queue-Pace-Id 헤더가 서비스에 전달된다")
	void applyFirstCome_withQueuePaceId() throws Exception {
		EntryApplyResponse response = new EntryApplyResponse(1L, EVENT_ID, "선점완료", null, null, null);
		given(entryService.apply(eq(USER_ID), eq(EVENT_ID), any(EntryCoursePaceRequest.class), eq(PACE_ID), eq(EventAppType.FIRST_COME)))
			.willReturn(response);

		mockMvc.perform(post("/events/{eventId}/entries/apply/first-come", EVENT_ID)
				.header(USER_ID_HEADER, USER_ID)
				.header("X-Queue-Pace-Id", PACE_ID)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new EntryCoursePaceRequest(COURSE_ID, PACE_ID))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		verify(entryService).apply(eq(USER_ID), eq(EVENT_ID), any(EntryCoursePaceRequest.class), eq(PACE_ID), eq(EventAppType.FIRST_COME));
	}

	@Test
	@DisplayName("재고 조회 성공 시 200과 재고 상태를 반환한다")
	void checkStock_success() throws Exception {
		given(entryService.checkStock(PACE_ID)).willReturn(EntryStockCheckResponse.available(5));

		mockMvc.perform(get("/events/{eventId}/entries/stock-check", EVENT_ID)
				.header(USER_ID_HEADER, USER_ID)
				.param("paceId", String.valueOf(PACE_ID)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.stockStatus").value("AVAILABLE"))
			.andExpect(jsonPath("$.data.remainingStock").value(5));
	}

}
