package com.kt.onrace.domain.mypage.dto;

import com.kt.onrace.domain.address.entity.Address;

/**
 * 기본 배송지 존재 여부와 기본 배송지 정보를 함께 반환하는 응답 DTO이다.
 * 주소가 없는 경우를 표현하는 빈 응답 팩토리도 제공한다.
 */
public record MyPageAddressResponseDto(
	boolean hasAddress,
	MyPageAddressDto defaultAddress
) {
	public static MyPageAddressResponseDto from(Address address) {
		return new MyPageAddressResponseDto(true, MyPageAddressDto.from(address));
	}

	public static MyPageAddressResponseDto empty() {
		return new MyPageAddressResponseDto(false, null);
	}
}
