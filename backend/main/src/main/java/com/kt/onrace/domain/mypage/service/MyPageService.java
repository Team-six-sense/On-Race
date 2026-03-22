package com.kt.onrace.domain.mypage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.domain.member.repository.MemberRepository;
import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderDetailResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderTab;
import com.kt.onrace.domain.mypage.dto.MyPageOverviewResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * MyPage는 생성/수정이 아니라 여러 도메인의 조회 결과를 화면용 응답으로 조합하는 진입점이다.
 */
public class MyPageService {

	private final MemberRepository memberRepository;
	private final MyPageQueryService myPageQueryService;
	private final ApplicationHistoryService applicationHistoryService;
	private final OrderHistoryService orderHistoryService;

	public MyPageOverviewResponseDto getOverview(Long userId) {
		validateMember(userId);
		return myPageQueryService.getOverview(userId);
	}

	public MyPageEntryListResponseDto getEntries(Long userId, int page, int size) {
		validateMember(userId);
		return applicationHistoryService.getEntries(userId, page, size);
	}

	public MyPageEntryListResponseDto getWaitingEntries(Long userId, int page, int size) {
		validateMember(userId);
		return applicationHistoryService.getWaitingEntries(userId, page, size);
	}

	public MyPageOrderListResponseDto getOrders(Long userId, String tab, int page, int size) {
		validateMember(userId);
		return orderHistoryService.getOrders(userId, MyPageOrderTab.from(tab), page, size);
	}

	public MyPageOrderDetailResponseDto getOrderDetail(Long userId, String orderNumber) {
		validateMember(userId);
		return orderHistoryService.getOrderDetail(userId, orderNumber);
	}

	public MyPageAddressResponseDto getAddress(Long userId) {
		validateMember(userId);
		return myPageQueryService.getAddress(userId);
	}

	private void validateMember(Long userId) {
		memberRepository.findByIdAndIsDeletedFalseOrThrow(userId, BusinessErrorCode.MEMBER_NOT_FOUND);
	}
}
