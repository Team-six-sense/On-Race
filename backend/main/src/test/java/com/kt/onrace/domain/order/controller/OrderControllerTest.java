package com.kt.onrace.domain.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.GlobalExceptionHandler;
import com.kt.onrace.common.filter.GatewayAccessFilter;
import com.kt.onrace.domain.order.dto.CheckoutRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutResponseDto;
import com.kt.onrace.domain.order.service.OrderService;

@WebMvcTest(
	controllers = OrderController.class,
	excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GatewayAccessFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

	private static final String USER_ID_HEADER = "X-User-Id";
	private static final Long USER_ID = 7L;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OrderService orderService;

	@Test
	@DisplayName("checkout 성공 시 200과 응답을 반환한다")
	void checkoutSuccess() throws Exception {
		given(orderService.checkout(eq(validRequest()), eq(USER_ID)))
			.willReturn(new CheckoutResponseDto("ORD-123", "서울 마라톤 하프코스", 63000L));

		mockMvc.perform(post("/orders/checkout")
				.header(USER_ID_HEADER, USER_ID)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validRequest())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.orderNumber").value("ORD-123"))
			.andExpect(jsonPath("$.data.amount").value(63000L));

		verify(orderService).checkout(any(CheckoutRequestDto.class), eq(USER_ID));
	}

	@Test
	@DisplayName("checkout은 필수 값이 누락되면 400을 반환한다")
	void checkoutRejectsInvalidRequest() throws Exception {
		String invalidJson = """
			{
			  "prepareToken": "",
			  "eventId": null,
			  "eventCourseId": 10,
			  "eventPaceId": 20,
			  "selectedPackageIds": [30],
			  "expectedFinalAmount": 63000,
			  "addressId": 101,
			  "deliveryMemo": "문앞에 놓아주세요"
			}
			""";

		mockMvc.perform(post("/orders/checkout")
				.header(USER_ID_HEADER, USER_ID)
				.contentType(APPLICATION_JSON)
				.content(invalidJson))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value(BusinessErrorCode.COMMON_INVALID_PARAMETER.getCode()))
			.andExpect(jsonPath("$.message").isNotEmpty());

		verifyNoInteractions(orderService);
	}

	private CheckoutRequestDto validRequest() {
		return new CheckoutRequestDto(
			"prepare-token",
			1L,
			10L,
			20L,
			java.util.List.of(30L),
			63000L,
			101L,
			"문앞에 놓아주세요"
		);
	}
}
