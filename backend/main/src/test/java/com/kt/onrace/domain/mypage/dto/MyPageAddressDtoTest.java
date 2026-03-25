package com.kt.onrace.domain.mypage.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kt.onrace.domain.address.dto.AddressDto;

class MyPageAddressDtoTest {

	@Test
	@DisplayName("address2가 없으면 address1만 trim 해서 사용한다")
	void composeAddressUsesOnlyPrimaryWhenAddress2IsBlank() {
		AddressDto.Response address = new AddressDto.Response(
			1L,
			"집",
			"홍길동",
			"01012345678",
			"06236",
			"  서울시 강남구  ",
			"   ",
			null,
			true
		);

		MyPageAddressDto summary = MyPageAddressDto.from(address);

		assertThat(summary.address()).isEqualTo("서울시 강남구");
	}

	@Test
	@DisplayName("address1과 address2가 모두 있으면 공백 한 칸으로 결합한 뒤 trim 한다")
	void composeAddressJoinsPrimaryAndSecondaryWithSingleSpace() {
		AddressDto.Response address = new AddressDto.Response(
			1L,
			"집",
			"홍길동",
			"01012345678",
			"06236",
			"  서울시 강남구  ",
			"  101동 1203호  ",
			null,
			true
		);

		MyPageAddressDto summary = MyPageAddressDto.from(address);

		assertThat(summary.address()).isEqualTo("서울시 강남구 101동 1203호");
	}
}
