package com.kt.onrace.domain.mypage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOverviewResponseDto;
import com.kt.onrace.domain.mypage.facade.MyPageFacade;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * MyPage는 생성/수정이 아니라 여러 도메인의 조회 결과를 화면용 응답으로 조합하는 진입점이다.
 */
public class MyPageService {

	private final MyPageFacade myPageFacade;

	public MyPageOverviewResponseDto getOverview(Long userId) {
		return myPageFacade.getOverview(userId);
	}

	public MyPageEntryListResponseDto getEntries(Long userId) {
		return myPageFacade.getEntries(userId);
	}

	public MyPageEntryListResponseDto getWaitingEntries(Long userId) {
		return myPageFacade.getWaitingEntries(userId);
	}

	public MyPageOrderListResponseDto getOrders(Long userId) {
		return myPageFacade.getOrders(userId);
	}

	public MyPageAddressResponseDto getAddress(Long userId) {
		return myPageFacade.getAddress(userId);
	}
}
