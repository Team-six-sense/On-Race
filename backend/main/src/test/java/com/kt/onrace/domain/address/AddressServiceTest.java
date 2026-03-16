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
		assertThat(address.getLabel()).isEqualTo("배송지1");
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
	@DisplayName("배송지 별칭 미입력 시 가장 작은 미사용 번호로 자동 생성된다")
	void createWithoutLabelGeneratesSmallestAvailableLabel() {
		addressService.create(1L, createRequest("첫주소", false, "배송지1"));
		addressService.create(1L, createRequest("둘주소", false, "배송지2"));
		addressService.create(1L, createRequest("셋주소", false, "배송지4"));

		AddressDto.Response response = addressService.create(1L, createRequest("넷주소", false));

		assertThat(response.label()).isEqualTo("배송지3");
	}

	@Test
	@DisplayName("배송지 별칭은 trim 후 저장된다")
	void createTrimsLabel() {
		AddressDto.Response response = addressService.create(1L, createRequest("첫주소", false, "  우리 집  "));

		assertThat(response.label()).isEqualTo("우리 집");
	}

	@Test
	@DisplayName("같은 사용자 내 배송지 별칭은 trim + 대소문자 무시 기준으로 중복될 수 없다")
	void duplicateLabelIsRejectedIgnoringTrimAndCase() {
		addressService.create(1L, createRequest("첫주소", false, "Office"));

		assertThatThrownBy(() -> addressService.create(1L, createRequest("둘주소", false, " office ")))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_DUPLICATE_LABEL);
	}

	@Test
	@DisplayName("배송지 별칭은 최대 20자, 한글 영문 숫자 공백만 허용한다")
	void invalidLabelFormatIsRejected() {
		assertThatThrownBy(() -> addressService.create(1L, createRequest("첫주소", false, "우리집!")))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_INVALID_LABEL);

		assertThatThrownBy(() -> addressService.create(1L, createRequest("둘주소", false, "abcdefghijklmnopqrstu")))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_INVALID_LABEL);
	}

	@Test
	@DisplayName("사용자당 배송지는 최대 10개까지만 등록할 수 있다")
	void cannotCreateMoreThanTenAddresses() {
		for (int i = 1; i <= 10; i++) {
			addressService.create(1L, createRequest("주소" + i, false));
		}

		assertThatThrownBy(() -> addressService.create(1L, createRequest("열한번째", false)))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_LIMIT_EXCEEDED);
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
	@DisplayName("기본배송지 조회는 기본배송지를 반환한다")
	void getDefaultReturnsDefaultAddress() {
		AddressDto.Response first = addressService.create(1L, createRequest("기본주소", false));
		addressService.create(1L, createRequest("둘주소", false));

		AddressDto.DefaultResponse response = addressService.getDefault(1L);

		assertThat(response.hasAddress()).isTrue();
		assertThat(response.address()).isNotNull();
		assertThat(response.address().id()).isEqualTo(first.id());
	}

	@Test
	@DisplayName("기본배송지 조회는 주소가 없으면 빈 상태를 반환한다")
	void getDefaultReturnsEmptyWhenNoAddress() {
		AddressDto.DefaultResponse response = addressService.getDefault(1L);

		assertThat(response.hasAddress()).isFalse();
		assertThat(response.address()).isNull();
	}

	@Test
	@DisplayName("기본배송지 삭제 시 남은 주소 중 최신 주소가 기본배송지로 승격된다")
	void deleteDefaultPromotesLatest() {
		AddressDto.Response first = addressService.create(1L, createRequest("기본주소", false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", false));
		AddressDto.Response third = addressService.create(1L, createRequest("셋주소", false));

		addressService.delete(1L, first.id());

		Address deletedAddress = addressRepository.findById(first.id()).orElseThrow();
		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();

		assertThat(deletedAddress.isDeleted()).isTrue();
		assertThat(deletedAddress.getDeletedAt()).isNotNull();
		assertThat(deletedAddress.isDefault()).isFalse();
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
	@DisplayName("기본배송지를 isDefault=false로 수정하면 다른 배송지가 기본으로 승격된다")
	void updateDefaultToNormalPromotesAnother() {
		AddressDto.Response first = addressService.create(1L, createRequest("기본주소", false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", false));

		addressService.update(1L, first.id(), createRequest("기본주소", false));

		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();
		assertThat(defaultAddress.getId()).isEqualTo(second.id());
	}

	@Test
	@DisplayName("update에서 label 미전송 시 기존 label을 유지한다")
	void updateKeepsExistingLabelWhenLabelIsMissing() {
		AddressDto.Response response = addressService.create(1L, createRequest("첫주소", false, "우리 집"));

		AddressDto.Response updated = addressService.update(1L, response.id(), createRequest("수정주소", false, null));

		assertThat(updated.label()).isEqualTo("우리 집");
	}

	@Test
	@DisplayName("update에서도 배송지 별칭 중복은 허용되지 않는다")
	void updateRejectsDuplicateLabel() {
		AddressDto.Response first = addressService.create(1L, createRequest("첫주소", false, "집"));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", false, "회사"));

		assertThatThrownBy(() -> addressService.update(1L, second.id(), createRequest("둘주소", false, " 집 ")))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_DUPLICATE_LABEL);
		assertThat(first.label()).isEqualTo("집");
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

	@Test
	@DisplayName("삭제된 배송지는 목록/조회/라벨 카운트에서 제외된다")
	void softDeletedAddressIsExcludedFromActiveQueries() {
		AddressDto.Response first = addressService.create(1L, createRequest("첫주소", false, "집"));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", false, "회사"));

		addressService.delete(1L, first.id());

		assertThat(addressService.list(1L)).extracting(AddressDto.Response::id)
			.containsExactly(second.id());
		assertThatThrownBy(() -> addressService.get(1L, first.id()))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_NOT_FOUND);

		AddressDto.Response recreated = addressService.create(1L, createRequest("셋주소", false, "집"));
		assertThat(recreated.label()).isEqualTo("집");
	}

	private AddressDto.SaveRequest createRequest(String receiverName, Boolean isDefault) {
		return createRequest(receiverName, isDefault, null);
	}

	private AddressDto.SaveRequest createRequest(String receiverName, Boolean isDefault, String label) {
		return new AddressDto.SaveRequest(
			receiverName,
			"010-1111-2222",
			"12345",
			"서울",
			"101동",
			"문앞",
			isDefault,
			label
		);
	}
}
