package com.kt.onrace.domain.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;
import com.kt.onrace.domain.mypage.client.AuthAccountClient;
import com.kt.onrace.domain.mypage.dto.MyPageAccountResponseDto;

@ExtendWith(MockitoExtension.class)
class MyPageAccountQueryServiceTest {

	@Mock
	private AuthAccountClient authAccountClient;

	@Mock
	private AddressRepository addressRepository;

	@InjectMocks
	private MyPageAddressQueryService myPageAddressQueryService;

	private MyPageAccountQueryService myPageAccountQueryService;

	@Test
	@DisplayName("계정 요약은 auth 원천값과 기본 배송지 요약을 함께 조립한다")
	void getAccountReturnsAccountSummary() {
		myPageAccountQueryService = new MyPageAccountQueryService(authAccountClient, myPageAddressQueryService);

		when(authAccountClient.getMyInfo(7L)).thenReturn(
			new AuthAccountClient.AccountSummary("홍길동", "01012345678", "LOCAL", "VERIFIED", true)
		);
		when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(7L)).thenReturn(List.of(defaultAddress()));

		MyPageAccountResponseDto response = myPageAccountQueryService.getAccount(7L);

		assertThat(response.name()).isEqualTo("홍길동");
		assertThat(response.phone()).isEqualTo("01012345678");
		assertThat(response.authProvider()).isEqualTo("LOCAL");
		assertThat(response.verificationStatus()).isEqualTo("VERIFIED");
		assertThat(response.marketingConsent()).isTrue();
		assertThat(response.address().hasAddress()).isTrue();
		assertThat(response.address().defaultAddress().label()).isEqualTo("집");
	}

	private Address defaultAddress() {
		return Address.builder()
			.userId(7L)
			.label("집")
			.normalizedLabel("집")
			.receiverName("홍길동")
			.phone("01012345678")
			.zipcode("06236")
			.address1("서울시 강남구")
			.address2("101동 1203호")
			.memo("문앞")
			.isDefault(true)
			.activeDefaultOwnerId(7L)
			.build();
	}
}
