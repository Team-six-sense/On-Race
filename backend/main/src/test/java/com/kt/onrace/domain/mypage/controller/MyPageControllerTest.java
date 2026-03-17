package com.kt.onrace.domain.mypage.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.GlobalExceptionHandler;
import com.kt.onrace.common.filter.GatewayAccessFilter;
import com.kt.onrace.domain.mypage.dto.MyPageAddressDto;
import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOverviewResponseDto;
import com.kt.onrace.domain.mypage.service.MyPageService;

@WebMvcTest(
	controllers = MyPageController.class,
	excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GatewayAccessFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MyPageControllerTest {

	private static final String USER_ID_HEADER = "X-User-Id";
	private static final Long USER_ID = 7L;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MyPageService myPageService;

	@Test
	void getOverviewReturnsAggregatedResponse() throws Exception {
		given(myPageService.getOverview(USER_ID)).willReturn(sampleOverview());

		mockMvc.perform(get("/mypage").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.entries.totalCount").value(1))
			.andExpect(jsonPath("$.data.waitingEntries.totalCount").value(0))
			.andExpect(jsonPath("$.data.orders.totalCount").value(1))
			.andExpect(jsonPath("$.data.address.hasAddress").value(true))
			.andExpect(jsonPath("$.data.address.defaultAddress.label").value("집"));

		verify(myPageService).getOverview(USER_ID);
	}

	@Test
	void getEntriesReturnsEntryList() throws Exception {
		given(myPageService.getEntries(USER_ID)).willReturn(sampleOverview().entries());

		mockMvc.perform(get("/mypage/entries").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.totalCount").value(1))
			.andExpect(jsonPath("$.data.items[0].title").value("서울 마라톤 대회 2026"));

		verify(myPageService).getEntries(USER_ID);
	}

	@Test
	void getWaitingEntriesReturnsWaitingList() throws Exception {
		given(myPageService.getWaitingEntries(USER_ID)).willReturn(MyPageEntryListResponseDto.empty());

		mockMvc.perform(get("/mypage/waiting-entries").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.totalCount").value(0))
			.andExpect(jsonPath("$.data.items").isArray());

		verify(myPageService).getWaitingEntries(USER_ID);
	}

	@Test
	void getOrdersReturnsOrderList() throws Exception {
		given(myPageService.getOrders(USER_ID)).willReturn(sampleOverview().orders());

		mockMvc.perform(get("/mypage/orders").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.totalCount").value(1))
			.andExpect(jsonPath("$.data.items[0].orderNumber").value("ORD-20260317-0001"));

		verify(myPageService).getOrders(USER_ID);
	}

	@Test
	void getAddressReturnsDefaultAddress() throws Exception {
		given(myPageService.getAddress(USER_ID)).willReturn(sampleOverview().address());

		mockMvc.perform(get("/mypage/address").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.hasAddress").value(true))
			.andExpect(jsonPath("$.data.defaultAddress.receiverName").value("홍길동"));

		verify(myPageService).getAddress(USER_ID);
	}

	@Test
	void missingUserIdHeaderReturnsInvalidParameter() throws Exception {
		mockMvc.perform(get("/mypage"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value(BusinessErrorCode.COMMON_INVALID_PARAMETER.getCode()))
			.andExpect(jsonPath("$.message").value(BusinessErrorCode.COMMON_INVALID_PARAMETER.getMessage()));

		verifyNoInteractions(myPageService);
	}

	private MyPageOverviewResponseDto sampleOverview() {
		MyPageEntryItemDto entryItem = new MyPageEntryItemDto(
			11L,
			101L,
			"APPLIED",
			"https://example.com/event-101.png",
			"서울 마라톤 대회 2026",
			"풀코스",
			"6:00/km",
			35000L,
			LocalDateTime.of(2026, 3, 10, 9, 0),
			LocalDateTime.of(2026, 3, 15, 12, 0)
		);

		MyPageOrderItemDto orderItem = new MyPageOrderItemDto(
			"ORD-20260317-0001",
			101L,
			"PENDING",
			"https://example.com/event-101.png",
			"서울 마라톤 대회 2026",
			"풀코스",
			"6:00/km",
			38000L,
			LocalDateTime.of(2026, 3, 17, 10, 0),
			LocalDateTime.of(2026, 3, 18, 23, 59)
		);

		MyPageAddressDto address = new MyPageAddressDto(
			1L,
			"집",
			"홍길동",
			"01012345678",
			"12345",
			"서울시 강남구",
			"101동",
			"문앞",
			true
		);

		return new MyPageOverviewResponseDto(
			new MyPageEntryListResponseDto(1, List.of(entryItem)),
			MyPageEntryListResponseDto.empty(),
			new MyPageOrderListResponseDto(1, List.of(orderItem)),
			new MyPageAddressResponseDto(true, address)
		);
	}
}
