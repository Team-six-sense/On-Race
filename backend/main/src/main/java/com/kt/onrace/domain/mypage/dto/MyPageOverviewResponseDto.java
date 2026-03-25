package com.kt.onrace.domain.mypage.dto;

/**
 * 마이페이지 메인 화면의 요약 데이터를 한 번에 전달하는 응답 DTO이다.
 * 신청 내역, 대기 신청 내역, 주문 내역, 기본 배송지 정보를 묶는다.
 */
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
