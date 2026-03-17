package com.kt.onrace.domain.mypage.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOverviewResponseDto;

@Component
@Transactional(readOnly = true)
/**
 * 마이페이지 전용 read model 조합 계층.
 * 이후 티켓에서 address/order/entry/event 조회를 여기에 모은다.
 */
public class MyPageFacade {

	public MyPageOverviewResponseDto getOverview(Long userId) {
		return new MyPageOverviewResponseDto(
			getEntries(userId),
			getWaitingEntries(userId),
			getOrders(userId),
			getAddress(userId)
		);
	}

	public MyPageEntryListResponseDto getEntries(Long userId) {
		return MyPageEntryListResponseDto.empty();
	}

	public MyPageEntryListResponseDto getWaitingEntries(Long userId) {
		return MyPageEntryListResponseDto.empty();
	}

	public MyPageOrderListResponseDto getOrders(Long userId) {
		return MyPageOrderListResponseDto.empty();
	}

	public MyPageAddressResponseDto getAddress(Long userId) {
		return MyPageAddressResponseDto.empty();
	}
}
