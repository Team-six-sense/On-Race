package com.kt.onrace.domain.mypage.dto;

import com.kt.onrace.domain.address.entity.Address;

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
