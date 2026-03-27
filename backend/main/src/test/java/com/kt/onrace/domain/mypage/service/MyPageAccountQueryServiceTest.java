package com.kt.onrace.domain.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kt.onrace.domain.address.dto.AddressDto;
import com.kt.onrace.domain.address.service.AddressService;
import com.kt.onrace.domain.mypage.client.AuthAccountClient;
import com.kt.onrace.domain.mypage.dto.MyPageAccountResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageAccountType;
import com.kt.onrace.domain.mypage.dto.MyPageVerificationStatus;

@ExtendWith(MockitoExtension.class)
class MyPageAccountQueryServiceTest {

	@Mock
	private AuthAccountClient authAccountClient;

	@Mock
	private AddressService addressService;

	private MyPageAddressQueryService myPageAddressQueryService;

	private MyPageAccountQueryService myPageAccountQueryService;

	@BeforeEach
	void setUp() {
		myPageAddressQueryService = new MyPageAddressQueryService(addressService);
		myPageAccountQueryService = new MyPageAccountQueryService(authAccountClient, myPageAddressQueryService);
	}

	@Test
	@DisplayName("계정 요약은 auth 원천값과 기본 배송지 요약을 함께 조립한다")
	void getAccountReturnsAccountSummary() {
		when(authAccountClient.getMyInfo(7L)).thenReturn(
			new AuthAccountClient.AccountSummary(
				"runner@example.com",
				"홍길동",
				"01012345678",
				"LOCAL",
				true,
				"VERIFIED",
				true
			)
		);
		when(addressService.getDefault(7L)).thenReturn(new AddressDto.DefaultResponse(true, defaultAddress()));

		MyPageAccountResponseDto response = myPageAccountQueryService.getAccount(7L);

		assertThat(response.accountType()).isEqualTo(MyPageAccountType.EMAIL);
		assertThat(response.name()).isEqualTo("홍길동");
		assertThat(response.phone()).isEqualTo("01012345678");
		assertThat(response.authProvider()).isEqualTo("LOCAL");
		assertThat(response.email()).isEqualTo("runner@example.com");
		assertThat(response.canChangePassword()).isTrue();
		assertThat(response.verificationStatus()).isEqualTo(MyPageVerificationStatus.COMPLETED);
		assertThat(response.marketingConsent()).isTrue();
		assertThat(response.address().hasAddress()).isTrue();
		assertThat(response.address().defaultAddress().receiverName()).isEqualTo("홍길동");
		assertThat(response.address().defaultAddress().label()).isEqualTo("집");
		assertThat(response.address().defaultAddress().address()).isEqualTo("서울시 강남구 101동 1203호");
		assertThat(response.address().defaultAddress().phone()).isEqualTo("01012345678");
	}

	@Test
	@DisplayName("기본 배송지가 없으면 hasAddress=false와 defaultAddress=null을 반환한다")
	void getAccountReturnsEmptyAddressWhenDefaultAddressMissing() {
		when(authAccountClient.getMyInfo(8L)).thenReturn(
			new AuthAccountClient.AccountSummary(
				"empty@example.com",
				"빈사용자",
				"01000000000",
				"LOCAL",
				true,
				"UNVERIFIED",
				false
			)
		);
		when(addressService.getDefault(8L)).thenReturn(AddressDto.DefaultResponse.empty());

		MyPageAccountResponseDto response = myPageAccountQueryService.getAccount(8L);

		assertThat(response.accountType()).isEqualTo(MyPageAccountType.EMAIL);
		assertThat(response.verificationStatus()).isEqualTo(MyPageVerificationStatus.PENDING);
		assertThat(response.address().hasAddress()).isFalse();
		assertThat(response.address().defaultAddress()).isNull();
	}

	@Test
	@DisplayName("SNS 계정은 accountType=SNS, canChangePassword=false, UNVERIFIED는 PENDING으로 매핑한다")
	void getAccountNormalizesSnsAccountFields() {
		when(authAccountClient.getMyInfo(9L)).thenReturn(
			new AuthAccountClient.AccountSummary(
				"sns@example.com",
				"카카오사용자",
				"01011112222",
				"KAKAO",
				false,
				"UNVERIFIED",
				true
			)
		);
		when(addressService.getDefault(9L)).thenReturn(new AddressDto.DefaultResponse(true, defaultAddress()));

		MyPageAccountResponseDto response = myPageAccountQueryService.getAccount(9L);

		assertThat(response.accountType()).isEqualTo(MyPageAccountType.SNS);
		assertThat(response.authProvider()).isEqualTo("KAKAO");
		assertThat(response.canChangePassword()).isFalse();
		assertThat(response.verificationStatus()).isEqualTo(MyPageVerificationStatus.PENDING);
	}

	private AddressDto.Response defaultAddress() {
		return new AddressDto.Response(
			1L,
			"집",
			"홍길동",
			"01012345678",
			"06236",
			"서울시 강남구",
			"101동 1203호",
			"문앞",
			true
		);
	}
}
