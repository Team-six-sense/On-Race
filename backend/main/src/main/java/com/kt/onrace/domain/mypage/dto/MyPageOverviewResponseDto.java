package com.kt.onrace.domain.mypage.dto;

public record MyPageOverviewResponseDto(
	MyPageEntryListResponseDto entries,
	MyPageEntryListResponseDto waitingEntries,
	MyPageOrderListResponseDto orders,
	MyPageAddressResponseDto address
) {
	public static MyPageOverviewResponseDto empty() {
		return new MyPageOverviewResponseDto(
			MyPageEntryListResponseDto.empty(0, 0),
			MyPageEntryListResponseDto.empty(0, 0),
			MyPageOrderListResponseDto.empty(0, 0),
			MyPageAddressResponseDto.empty()
		);
	}
}
