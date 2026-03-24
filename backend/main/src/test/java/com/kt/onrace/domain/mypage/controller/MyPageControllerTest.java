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
import com.kt.onrace.domain.mypage.dto.MyPageAccountResponse;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryFilter;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderDetailResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOverviewResponseDto;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventStatus;
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
	void getAccountReturnsMyPageAccountResponse() throws Exception {
		given(myPageService.getAccount(USER_ID)).willReturn(sampleAccount());

		mockMvc.perform(get("/mypage/account").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.name").value("김유저"))
			.andExpect(jsonPath("$.data.email").value("user@test.com"))
			.andExpect(jsonPath("$.data.phone").value("01012345678"))
			.andExpect(jsonPath("$.data.canChangePassword").value(true))
			.andExpect(jsonPath("$.data.verificationStatus").value("COMPLETED"))
			.andExpect(jsonPath("$.data.marketingConsent").value(true));

		verify(myPageService).getAccount(USER_ID);
	}

	@Test
	void getEntriesReturnsEntryList() throws Exception {
		given(myPageService.getApplicationHistory(USER_ID, MyPageApplicationHistoryFilter.ALL, 0, 20))
			.willReturn(sampleApplicationHistory(MyPageApplicationHistoryFilter.ALL));

		mockMvc.perform(get("/mypage/entries").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.filter").value("ALL"))
			.andExpect(jsonPath("$.data.empty").value(false))
			.andExpect(jsonPath("$.data.pagination.page").value(0))
			.andExpect(jsonPath("$.data.pagination.size").value(20))
			.andExpect(jsonPath("$.data.pagination.totalCount").value(1))
			.andExpect(jsonPath("$.data.pagination.hasNext").value(false))
			.andExpect(jsonPath("$.data.items[0].eventName").value("서울 마라톤 대회 2026"))
			.andExpect(jsonPath("$.data.items[0].applicationType").value("LOTTERY"))
			.andExpect(jsonPath("$.data.items[0].displayStatus").value("응모 완료"))
			.andExpect(jsonPath("$.data.items[0].deepLink").value("/ticketing/101"))
			.andExpect(jsonPath("$.data.items[0].thumbnailUrl").value("https://example.com/event-101.png"));

		verify(myPageService).getApplicationHistory(USER_ID, MyPageApplicationHistoryFilter.ALL, 0, 20);
	}

	@Test
	void getEntriesAppliesFilter() throws Exception {
		given(myPageService.getApplicationHistory(USER_ID, MyPageApplicationHistoryFilter.LOTTERY, 1, 10))
			.willReturn(sampleApplicationHistory(MyPageApplicationHistoryFilter.LOTTERY));

		mockMvc.perform(
			get("/mypage/entries")
				.header(USER_ID_HEADER, USER_ID)
				.param("filter", "LOTTERY")
				.param("page", "1")
				.param("size", "10")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.filter").value("LOTTERY"));

		verify(myPageService).getApplicationHistory(USER_ID, MyPageApplicationHistoryFilter.LOTTERY, 1, 10);
	}

	@Test
	void getEntriesRejectsInvalidFilter() throws Exception {
		mockMvc.perform(
			get("/mypage/entries")
				.header(USER_ID_HEADER, USER_ID)
				.param("filter", "INVALID")
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value(BusinessErrorCode.COMMON_INVALID_PARAMETER.getCode()))
			.andExpect(jsonPath("$.message").value(BusinessErrorCode.COMMON_INVALID_PARAMETER.getMessage()));

		verifyNoInteractions(myPageService);
	}

	@Test
	void getWaitingEntriesReturnsWaitingList() throws Exception {
		given(myPageService.getWaitingEntries(USER_ID, 0, 20)).willReturn(MyPageEntryListResponseDto.empty(0, 20));

		mockMvc.perform(get("/mypage/waiting-entries").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.page").value(0))
			.andExpect(jsonPath("$.data.size").value(20))
			.andExpect(jsonPath("$.data.totalCount").value(0))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.items").isArray());

		verify(myPageService).getWaitingEntries(USER_ID, 0, 20);
	}

	@Test
	void getOrdersReturnsOrderList() throws Exception {
		given(myPageService.getOrders(USER_ID, "ALL", 0, 20)).willReturn(sampleOverview().orders());

		mockMvc.perform(get("/mypage/orders").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.page").value(0))
			.andExpect(jsonPath("$.data.size").value(3))
			.andExpect(jsonPath("$.data.totalCount").value(1))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.items[0].orderNumber").value("ORD-20260317-0001"));

		verify(myPageService).getOrders(USER_ID, "ALL", 0, 20);
	}

	@Test
	void getOrderDetailReturnsDetailResponse() throws Exception {
		given(myPageService.getOrderDetail(USER_ID, "ORD-20260317-0001")).willReturn(sampleOrderDetail());

		mockMvc.perform(get("/mypage/orders/ORD-20260317-0001").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.orderNumber").value("ORD-20260317-0001"))
			.andExpect(jsonPath("$.data.status").value("결제 대기"))
			.andExpect(jsonPath("$.data.canCancel").value(true))
			.andExpect(jsonPath("$.data.packages[0].name").value("기록칩"));

		verify(myPageService).getOrderDetail(USER_ID, "ORD-20260317-0001");
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
			"응모 완료",
			"NONE",
			null,
			false,
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
			"결제 대기",
			"DETAIL",
			"주문 상세보기",
			true,
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
			new MyPageEntryListResponseDto(0, 3, 1, false, List.of(entryItem)),
			MyPageEntryListResponseDto.empty(0, 3),
			new MyPageOrderListResponseDto(0, 3, 1, false, List.of(orderItem)),
			new MyPageAddressResponseDto(true, address)
		);
	}

	private MyPageAccountResponse sampleAccount() {
		return new MyPageAccountResponse(
			"김유저",
			"user@test.com",
			"01012345678",
			true,
			"COMPLETED",
			true
		);
	}

	private MyPageApplicationHistoryResponseDto sampleApplicationHistory(MyPageApplicationHistoryFilter filter) {
		MyPageApplicationHistoryItemDto item = new MyPageApplicationHistoryItemDto(
			11L,
			101L,
			"서울 마라톤 대회 2026",
			EventAppType.LOTTERY,
			EventStatus.IN_PROGRESS,
			"응모 완료",
			"NONE",
			null,
			false,
			"/ticketing/101",
			"https://example.com/event-101.png",
			"풀코스",
			"6:00/km",
			35000L,
			LocalDateTime.of(2026, 3, 10, 9, 0),
			LocalDateTime.of(2026, 4, 15, 9, 0),
			LocalDateTime.of(2026, 3, 1, 9, 0),
			LocalDateTime.of(2026, 3, 20, 18, 0),
			LocalDateTime.of(2026, 3, 25, 12, 0)
		);

		return MyPageApplicationHistoryResponseDto.of(filter, 0, 20, 1, List.of(item));
	}

	private MyPageOrderDetailResponseDto sampleOrderDetail() {
		return new MyPageOrderDetailResponseDto(
			101L,
			"ORD-20260317-0001",
			"결제 대기",
			"DETAIL",
			"주문 상세보기",
			true,
			LocalDateTime.of(2026, 3, 17, 10, 0),
			null,
			"서울 마라톤 대회 2026",
			null,
			"풀코스",
			"6:00/km",
			35000L,
			3000L,
			0L,
			38000L,
			"홍길동",
			"집",
			"01012345678",
			"12345",
			"서울시 강남구",
			"101동",
			"문앞",
			null,
			false,
			null,
			null,
			true,
			false,
			false,
			List.of(new MyPageOrderDetailResponseDto.PackageInfo(1L, "기록칩", 5000L))
		);
	}
}
