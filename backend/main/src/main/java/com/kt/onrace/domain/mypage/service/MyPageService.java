package com.kt.onrace.domain.mypage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.domain.member.repository.MemberRepository;
import com.kt.onrace.domain.mypage.dto.MyPageAccountResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderDetailResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderTab;
import com.kt.onrace.domain.mypage.dto.MyPageOverviewResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * 마이페이지 조회 기능의 진입점 서비스이다.
 * 회원 존재 여부를 검증한 뒤 개별 조회 서비스를 조합해 화면용 응답을 반환한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

	private final MemberRepository memberRepository;
	private final MyPageAccountQueryService myPageAccountQueryService;
	private final MyPageQueryService myPageQueryService;
	private final ApplicationHistoryService applicationHistoryService;
	private final OrderHistoryService orderHistoryService;

	public MyPageAccountResponseDto getAccount(Long userId) {
		validateMember(userId);
		return myPageAccountQueryService.getAccount(userId);
	}

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
