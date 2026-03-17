package com.kt.onrace.domain.mypage.dto;

public record MyPageAddressResponseDto(
	boolean hasAddress,
	MyPageAddressDto defaultAddress
) {
	public static MyPageAddressResponseDto empty() {
		return new MyPageAddressResponseDto(false, null);
	}
}
