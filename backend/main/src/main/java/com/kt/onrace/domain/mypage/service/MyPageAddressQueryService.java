package com.kt.onrace.domain.mypage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.domain.address.service.AddressService;
import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * 마이페이지에서 기본 배송지 요약을 조회하는 서비스이다.
 * 기본 배송지 조회, 주소 없음 빈 상태, 비정상 기본배송지 복구는 기존 address 도메인 정책을 재사용한다.
 */
@ServiceLog
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageAddressQueryService {

	private final AddressService addressService;

	public MyPageAddressResponseDto getAddress(Long userId) {
		var defaultAddress = addressService.getDefault(userId);
		if (!defaultAddress.hasAddress() || defaultAddress.address() == null) {
			return MyPageAddressResponseDto.empty();
		}

		return MyPageAddressResponseDto.from(defaultAddress.address());
	}
}
