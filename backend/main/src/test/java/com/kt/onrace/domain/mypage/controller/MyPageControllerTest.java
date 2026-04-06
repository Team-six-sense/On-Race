package com.kt.onrace.domain.mypage.controller;

import static org.hamcrest.Matchers.nullValue;
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
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.kt.onrace.domain.mypage.dto.MyPageAccountResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageAddressDto;
import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderDetailResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPagePaymentHistoryItemDto;
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
	void getAccountReturnsAccountSummary() throws Exception {
		given(myPageService.getAccount(USER_ID)).willReturn(sampleAccount());

		mockMvc.perform(get("/mypage/account").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.id").value(7))
			.andExpect(jsonPath("$.data.name").value("홍길동"))
			.andExpect(jsonPath("$.data.phoneNumber").value("01012345678"))
			.andExpect(jsonPath("$.data.email").value("runner@example.com"))
			.andExpect(jsonPath("$.data.isPassAuth").value(true))
			.andExpect(jsonPath("$.data.addressList[0].label").value("집"))
			.andExpect(jsonPath("$.data.addressList[0].receiverName").value("홍길동"))
			.andExpect(jsonPath("$.data.addressList[0].phoneNumber").value("01012345678"))
			.andExpect(jsonPath("$.data.addressList[0].address1").value("서울시 강남구"))
			.andExpect(jsonPath("$.data.addressList[1].label").value("회사"))
			.andExpect(jsonPath("$.data.addressList[1].isDefault").value(false));

		verify(myPageService).getAccount(USER_ID);
	}

	@Test
	void getOverviewReturnsAggregatedResponse() throws Exception {
		given(myPageService.getOverview(USER_ID)).willReturn(sampleOverview());

		mockMvc.perform(get("/mypage").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.entries.totalCount").value(1))
			.andExpect(jsonPath("$.data.entries.items[0].courseName").value("풀코스"))
			.andExpect(jsonPath("$.data.entries.items[0].paceName").value("6:00/km"))
			.andExpect(jsonPath("$.data.waitingEntries.totalCount").value(0))
			.andExpect(jsonPath("$.data.orders.totalCount").value(1))
			.andExpect(jsonPath("$.data.address.hasAddress").value(true))
			.andExpect(jsonPath("$.data.address.defaultAddress.label").value("집"));

		verify(myPageService).getOverview(USER_ID);
	}

	@Test
	void getEntriesReturnsEntryList() throws Exception {
		given(myPageService.getEntries(USER_ID, "ALL")).willReturn(sampleApplicationHistory());

		mockMvc.perform(get("/mypage/entries").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].id").value(11))
			.andExpect(jsonPath("$.data[0].eventId").value(101))
			.andExpect(jsonPath("$.data[0].title").value("서울 마라톤 대회 2026"))
			.andExpect(jsonPath("$.data[0].appType").value("LOTTERY"))
			.andExpect(jsonPath("$.data[0].status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.data[0].entryStatus").value("응모 완료"))
			.andExpect(jsonPath("$.data[0].resultAt").value("2026-03-16T12:00:00"))
			.andExpect(jsonPath("$.data[0].venue").value("잠실종합운동장"))
			.andExpect(jsonPath("$.data[0].course").value("풀코스"))
			.andExpect(jsonPath("$.data[0].pace").value("6:00/km"));

		verify(myPageService).getEntries(USER_ID, "ALL");
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
		given(myPageService.getOrders(USER_ID, "ALL")).willReturn(samplePaymentHistories());

		mockMvc.perform(get("/mypage/orders").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].id").value("ORD-20260317-0001"))
			.andExpect(jsonPath("$.data[0].eventId").value(101))
			.andExpect(jsonPath("$.data[0].title").value("서울 마라톤 대회 2026"))
			.andExpect(jsonPath("$.data[0].appType").value("LOTTERY"))
			.andExpect(jsonPath("$.data[0].status").value("END"))
			.andExpect(jsonPath("$.data[0].orderStatus").value("입금대기"))
			.andExpect(jsonPath("$.data[0].venue").value("잠실종합운동장"))
			.andExpect(jsonPath("$.data[0].course").value("풀코스"))
			.andExpect(jsonPath("$.data[0].pace").value("6:00/km"))
			.andExpect(jsonPath("$.data[0].price").value(38000));

		verify(myPageService).getOrders(USER_ID, "ALL");
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
			.andExpect(jsonPath("$.data.defaultAddress.receiverName").value("홍길동"))
			.andExpect(jsonPath("$.data.defaultAddress.address").value("서울시 강남구 101동"));

		verify(myPageService).getAddress(USER_ID);
	}

	@Test
	void getAddressReturnsNullDefaultAddressWhenUserHasNoAddress() throws Exception {
		given(myPageService.getAddress(USER_ID)).willReturn(MyPageAddressResponseDto.empty());

		mockMvc.perform(get("/mypage/address").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.hasAddress").value(false))
			.andExpect(jsonPath("$.data.defaultAddress").value(nullValue()));

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
			"https://example.com/event-101.png",
			"서울 마라톤 대회 2026",
			"풀코스",
			"6:00/km",
			38000L,
			LocalDateTime.of(2026, 3, 17, 10, 0),
			LocalDateTime.of(2026, 3, 18, 23, 59)
		);

		MyPageAddressDto address = new MyPageAddressDto(
			"홍길동",
			"집",
			"서울시 강남구 101동",
			"01012345678"
		);

		return new MyPageOverviewResponseDto(
			new MyPageEntryListResponseDto(0, 3, 1, false, List.of(entryItem)),
			MyPageEntryListResponseDto.empty(0, 3),
			new MyPageOrderListResponseDto(0, 3, 1, false, List.of(orderItem)),
			new MyPageAddressResponseDto(true, address)
		);
	}

	private List<MyPagePaymentHistoryItemDto> samplePaymentHistories() {
		return List.of(new MyPagePaymentHistoryItemDto(
			"ORD-20260317-0001",
			101L,
			"서울 마라톤 대회 2026",
			EventAppType.LOTTERY,
			EventStatus.END,
			"입금대기",
			LocalDateTime.of(2026, 3, 17, 10, 0),
			LocalDateTime.of(2026, 3, 15, 9, 0),
			"잠실종합운동장",
			"풀코스",
			"6:00/km",
			38000L
		));
	}

	private List<MyPageApplicationHistoryItemDto> sampleApplicationHistory() {
		return List.of(new MyPageApplicationHistoryItemDto(
			11L,
			101L,
			"서울 마라톤 대회 2026",
			EventAppType.LOTTERY,
			EventStatus.IN_PROGRESS,
			"응모 완료",
			LocalDateTime.of(2026, 3, 10, 9, 0),
			LocalDateTime.of(2026, 3, 15, 9, 0),
			LocalDateTime.of(2026, 3, 1, 0, 0),
			LocalDateTime.of(2026, 3, 14, 23, 59),
			LocalDateTime.of(2026, 3, 16, 12, 0),
			"잠실종합운동장",
			"풀코스",
			"6:00/km"
		));
	}

	private MyPageAccountResponseDto sampleAccount() {
		return new MyPageAccountResponseDto(
			7L,
			"홍길동",
			"01012345678",
			"runner@example.com",
			true,
			List.of(
				new MyPageAccountResponseDto.AddressItem(
					1L,
					"집",
					"홍길동",
					"01012345678",
					"12345",
					"서울시 강남구",
					"101동",
					"문앞",
					true
				),
				new MyPageAccountResponseDto.AddressItem(
					2L,
					"회사",
					"홍길동",
					"01099998888",
					"54321",
					"서울시 강남구",
					"좋은아파트 102동 304호",
					"경비실",
					false
				)
			)
		);
	}

	private MyPageOrderDetailResponseDto sampleOrderDetail() {
		return new MyPageOrderDetailResponseDto(
			101L,
			"ORD-20260317-0001",
			"결제 대기",
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
