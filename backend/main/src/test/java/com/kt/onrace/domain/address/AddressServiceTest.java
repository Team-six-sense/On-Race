package com.kt.onrace.domain.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kt.onrace.common.config.JpaAuditingConfig;
import com.kt.onrace.common.config.QueryDslConfig;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.address.dto.AddressDto;
import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;
import com.kt.onrace.domain.address.service.AddressService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({AddressService.class, JpaAuditingConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
class AddressServiceTest {

	@Autowired
	private AddressService addressService;

	@Autowired
	private AddressRepository addressRepository;

	@Test
	@DisplayName("첫 배송지는 isDefault=false여도 자동 기본배송지로 설정된다")
	void createFirstAddressBecomesDefault() {
		AddressDto.Response response = addressService.create(1L, createRequest("홍길동", false));

		Address address = addressRepository.findById(response.id()).orElseThrow();
		assertThat(address.isDefault()).isTrue();
	}

	@Test
	@DisplayName("두 번째 배송지 생성 시 isDefault가 false면 기존 기본배송지를 유지한다")
	void createSecondAddressKeepsDefault() {
		AddressDto.Response first = addressService.create(1L, createRequest("첫주소", false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", false));

		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();
		assertThat(defaultAddress.getId()).isEqualTo(first.id());

		List<Address> all = addressRepository.findByUserIdOrderByCreatedAtDesc(1L);
		assertThat(all).extracting(Address::getId).containsExactly(second.id(), first.id());
	}

	@Test
	@DisplayName("두 번째 배송지 생성 시 isDefault=true면 기존 기본배송지를 해제한다")
	void createSecondAddressReplacesDefault() {
		addressService.create(1L, createRequest("첫주소", false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", true));

		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();
		assertThat(defaultAddress.getId()).isEqualTo(second.id());
	}

	@Test
	@DisplayName("목록은 기본배송지 우선, 그 다음 최신순으로 정렬된다")
	void listOrdersDefaultFirstThenLatest() {
		AddressDto.Response first = addressService.create(1L, createRequest("기본주소", false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", false));
		AddressDto.Response third = addressService.create(1L, createRequest("셋주소", false));

		List<AddressDto.Response> list = addressService.list(1L);
		assertThat(list).extracting(AddressDto.Response::id)
			.containsExactly(first.id(), third.id(), second.id());
	}

	@Test
	@DisplayName("기본배송지 삭제 시 남은 주소 중 최신 주소가 기본배송지로 승격된다")
	void deleteDefaultPromotesLatest() {
		AddressDto.Response first = addressService.create(1L, createRequest("기본주소", false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", false));
		AddressDto.Response third = addressService.create(1L, createRequest("셋주소", false));

		addressService.delete(1L, first.id());

		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();
		assertThat(defaultAddress.getId()).isEqualTo(third.id());
		assertThat(defaultAddress.getId()).isNotEqualTo(second.id());
	}

	@Test
	@DisplayName("setDefault 호출 시 기존 기본배송지를 해제하고 대상 주소를 기본으로 설정한다")
	void setDefaultUpdatesDefaultAddress() {
		AddressDto.Response first = addressService.create(1L, createRequest("첫주소", false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", false));

		addressService.setDefault(1L, second.id());

		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();
		assertThat(defaultAddress.getId()).isEqualTo(second.id());
		assertThat(defaultAddress.getId()).isNotEqualTo(first.id());
	}

	@Test
	@DisplayName("다른 유저의 배송지 조회는 NOT_FOUND 예외가 발생한다")
	void getAddressOfAnotherUserReturnsNotFound() {
		AddressDto.Response response = addressService.create(1L, createRequest("첫주소", false));

		assertThatThrownBy(() -> addressService.get(2L, response.id()))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_NOT_FOUND);
	}

	private AddressDto.CreateRequest createRequest(String receiverName, Boolean isDefault) {
		return new AddressDto.CreateRequest(
			receiverName,
			"010-1111-2222",
			"12345",
			"서울",
			"101동",
			"문앞",
			isDefault
		);
	}
}
