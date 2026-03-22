package com.kt.onrace.domain.mypage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;
import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderTab;
import com.kt.onrace.domain.mypage.dto.MyPageOverviewResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * 마이페이지 상단 요약 정보와 기본 배송지 정보를 조회하는 서비스이다.
 * 신청, 주문, 주소 데이터를 묶어 메인 화면 응답으로 구성한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageQueryService {

	private final AddressRepository addressRepository;
	private final ApplicationHistoryService applicationHistoryService;
	private final OrderHistoryService orderHistoryService;

	public MyPageOverviewResponseDto getOverview(Long userId) {
		return new MyPageOverviewResponseDto(
			applicationHistoryService.getEntries(userId, MyPagePagingPolicy.DEFAULT_PAGE, MyPagePagingPolicy.SUMMARY_SIZE),
			applicationHistoryService.getWaitingEntries(userId, MyPagePagingPolicy.DEFAULT_PAGE, MyPagePagingPolicy.SUMMARY_SIZE),
			orderHistoryService.getOrders(userId, MyPageOrderTab.ALL, MyPagePagingPolicy.DEFAULT_PAGE, MyPagePagingPolicy.SUMMARY_SIZE),
			getAddress(userId)
		);
	}

	public MyPageAddressResponseDto getAddress(Long userId) {
		List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
		if (addresses.isEmpty()) {
			return MyPageAddressResponseDto.empty();
		}

		Address defaultAddress = addresses.stream()
			.filter(Address::isDefault)
			.findFirst()
			.orElse(addresses.get(0));

		return MyPageAddressResponseDto.from(defaultAddress);
	}
}
