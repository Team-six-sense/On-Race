package com.kt.onrace.domain.mypage.dto;

public record MyPageOverviewResponseDto(
	MyPageEntryListResponseDto entries,
	MyPageEntryListResponseDto waitingEntries,
	MyPageOrderListResponseDto orders,
	MyPageAddressResponseDto address
) {
	public static MyPageOverviewResponseDto empty() {
		return new MyPageOverviewResponseDto(
			MyPageEntryListResponseDto.empty(),
			MyPageEntryListResponseDto.empty(),
			MyPageOrderListResponseDto.empty(),
			MyPageAddressResponseDto.empty()
		);
	}
}
