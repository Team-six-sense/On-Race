package com.kt.onrace.domain.mypage.dto;

import com.kt.onrace.domain.address.dto.AddressDto;

/**
 * 기본 배송지 존재 여부와 기본 배송지 정보를 함께 반환하는 응답 DTO이다.
 * 주소가 없는 사용자는 hasAddress=false, defaultAddress=null 로 표현한다.
 */
public record MyPageAddressResponseDto(
	boolean hasAddress,
	MyPageAddressDto defaultAddress
) {
	public static MyPageAddressResponseDto from(AddressDto.Response address) {
		return new MyPageAddressResponseDto(true, MyPageAddressDto.from(address));
	}

	public static MyPageAddressResponseDto empty() {
		return new MyPageAddressResponseDto(false, null);
	}
}
