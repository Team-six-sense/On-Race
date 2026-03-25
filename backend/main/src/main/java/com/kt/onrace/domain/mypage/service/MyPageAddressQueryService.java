package com.kt.onrace.domain.mypage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;
import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * 마이페이지에서 기본 배송지 요약을 조회하는 서비스이다.
 * 활성 주소가 존재하면 기본 배송지를 우선 조회하고, 없으면 최신 주소를 fallback으로 사용한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageAddressQueryService {

	private final AddressRepository addressRepository;

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
