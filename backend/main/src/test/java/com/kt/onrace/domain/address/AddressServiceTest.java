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
		AddressDto.Response response = addressService.create(1L, createRequest("홍길동", null, false));

		Address address = addressRepository.findById(response.id()).orElseThrow();
		assertThat(address.isDefault()).isTrue();
		assertThat(address.getLabel()).isEqualTo("배송지1");
	}

	@Test
	@DisplayName("두 번째 배송지 생성 시 isDefault가 false면 기존 기본배송지를 유지한다")
	void createSecondAddressKeepsDefault() {
		AddressDto.Response first = addressService.create(1L, createRequest("첫주소", null, false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", null, false));

		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();
		assertThat(defaultAddress.getId()).isEqualTo(first.id());

		List<Address> all = addressRepository.findByUserIdOrderByCreatedAtDesc(1L);
		assertThat(all).extracting(Address::getId).containsExactly(second.id(), first.id());
	}

	@Test
	@DisplayName("두 번째 배송지 생성 시 isDefault=true면 기존 기본배송지를 해제한다")
	void createSecondAddressReplacesDefault() {
		addressService.create(1L, createRequest("첫주소", null, false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", null, true));

		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();
		assertThat(defaultAddress.getId()).isEqualTo(second.id());
	}

	@Test
	@DisplayName("목록은 기본배송지 우선, 그 다음 최신순으로 정렬된다")
	void listOrdersDefaultFirstThenLatest() {
		AddressDto.Response first = addressService.create(1L, createRequest("기본주소", null, false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", null, false));
		AddressDto.Response third = addressService.create(1L, createRequest("셋주소", null, false));

		List<AddressDto.Response> list = addressService.list(1L);
		assertThat(list).extracting(AddressDto.Response::id)
			.containsExactly(first.id(), third.id(), second.id());
	}

	@Test
	@DisplayName("기본배송지 삭제 시 남은 주소 중 최신 주소가 기본배송지로 승격된다")
	void deleteDefaultPromotesLatest() {
		AddressDto.Response first = addressService.create(1L, createRequest("기본주소", null, false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", null, false));
		AddressDto.Response third = addressService.create(1L, createRequest("셋주소", null, false));

		addressService.delete(1L, first.id());

		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();
		assertThat(defaultAddress.getId()).isEqualTo(third.id());
		assertThat(defaultAddress.getId()).isNotEqualTo(second.id());
	}

	@Test
	@DisplayName("setDefault 호출 시 기존 기본배송지를 해제하고 대상 주소를 기본으로 설정한다")
	void setDefaultUpdatesDefaultAddress() {
		AddressDto.Response first = addressService.create(1L, createRequest("첫주소", null, false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", null, false));

		addressService.setDefault(1L, second.id());

		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();
		assertThat(defaultAddress.getId()).isEqualTo(second.id());
		assertThat(defaultAddress.getId()).isNotEqualTo(first.id());
	}

	@Test
	@DisplayName("기본배송지를 isDefault=false로 수정하면 다른 배송지가 기본으로 승격된다")
	void updateDefaultToNormalPromotesAnother() {
		AddressDto.Response first = addressService.create(1L, createRequest("기본주소", null, false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", null, false));

		addressService.update(1L, first.id(), createRequest("기본주소", null, false));

		Address defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(1L).orElseThrow();
		assertThat(defaultAddress.getId()).isEqualTo(second.id());
	}

	@Test
	@DisplayName("다른 유저의 배송지 조회는 NOT_FOUND 예외가 발생한다")
	void getAddressOfAnotherUserReturnsNotFound() {
		AddressDto.Response response = addressService.create(1L, createRequest("첫주소", null, false));

		assertThatThrownBy(() -> addressService.get(2L, response.id()))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_NOT_FOUND);
	}

	@Test
	@DisplayName("라벨 미입력 시 가장 작은 미사용 배송지 번호를 자동 생성한다")
	void createWithoutLabelUsesSmallestUnusedNumber() {
		AddressDto.Response first = addressService.create(1L, createRequest("첫주소", "배송지1", false));
		AddressDto.Response second = addressService.create(1L, createRequest("둘주소", "배송지2", false));
		AddressDto.Response third = addressService.create(1L, createRequest("셋주소", "배송지3", false));

		addressService.delete(1L, second.id());

		AddressDto.Response recreated = addressService.create(1L, createRequest("넷주소", null, false));

		assertThat(first.label()).isEqualTo("배송지1");
		assertThat(third.label()).isEqualTo("배송지3");
		assertThat(recreated.label()).isEqualTo("배송지2");
	}

	@Test
	@DisplayName("큰 숫자가 포함된 배송지 라벨이 있어도 자동 라벨 생성이 실패하지 않는다")
	void createWithoutLabelIgnoresVeryLargeAutoLabelNumber() {
		addressService.create(1L, createRequest("큰번호주소", "배송지99999999999999999", false));

		AddressDto.Response response = addressService.create(1L, createRequest("일반주소", null, false));

		assertThat(response.label()).isEqualTo("배송지1");
	}

	@Test
	@DisplayName("입력 라벨은 trim 후 저장된다")
	void createLabelStoresTrimmedValue() {
		AddressDto.Response response = addressService.create(1L, createRequest("회사주소", " 회사 ", false));

		assertThat(response.label()).isEqualTo("회사");
	}

	@Test
	@DisplayName("생성 시 라벨에 빈칸만 입력하면 자동 라벨을 생성한다")
	void createWithWhitespaceOnlyLabelGeneratesAutoLabel() {
		AddressDto.Response response = addressService.create(1L, createRequest("회사주소", "   ", false));

		assertThat(response.label()).isEqualTo("배송지1");
	}

	@Test
	@DisplayName("라벨은 trim 후 중복 비교한다")
	void createLabelRejectsTrimmedDuplicate() {
		addressService.create(1L, createRequest("집주소", "집", false));

		assertThatThrownBy(() -> addressService.create(1L, createRequest("다른집", " 집 ", false)))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_DUPLICATE_LABEL);
	}

	@Test
	@DisplayName("영문 라벨은 대소문자를 무시하고 중복 비교한다")
	void createLabelRejectsCaseInsensitiveDuplicate() {
		addressService.create(1L, createRequest("회사주소", "HOME", false));

		assertThatThrownBy(() -> addressService.create(1L, createRequest("다른회사", "home", false)))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_DUPLICATE_LABEL);
	}

	@Test
	@DisplayName("수정 시 빈 라벨을 입력하면 기존 라벨을 유지한다")
	void updateWithBlankLabelKeepsExistingLabel() {
		AddressDto.Response created = addressService.create(1L, createRequest("집주소", "집", false));

		AddressDto.Response updated = addressService.update(1L, created.id(), createRequest("집주소수정", "", null));

		assertThat(updated.label()).isEqualTo("집");
	}

	@Test
	@DisplayName("수정 시 라벨에 빈칸만 입력하면 기존 라벨을 유지한다")
	void updateWithWhitespaceOnlyLabelKeepsExistingLabel() {
		AddressDto.Response created = addressService.create(1L, createRequest("집주소", "집", false));

		AddressDto.Response updated = addressService.update(1L, created.id(), createRequest("집주소수정", "   ", null));

		assertThat(updated.label()).isEqualTo("집");
	}

	@Test
	@DisplayName("수정 시 입력 라벨은 trim 후 저장된다")
	void updateLabelStoresTrimmedValue() {
		AddressDto.Response created = addressService.create(1L, createRequest("회사주소", "회사", false));

		AddressDto.Response updated = addressService.update(1L, created.id(), createRequest("회사주소수정", " 사무실 ", null));

		assertThat(updated.label()).isEqualTo("사무실");
	}

	@Test
	@DisplayName("수정 시 다른 배송지와 중복되는 라벨은 허용되지 않는다")
	void updateLabelRejectsDuplicateOfAnotherAddress() {
		addressService.create(1L, createRequest("집주소", "집", false));
		AddressDto.Response office = addressService.create(1L, createRequest("회사주소", "회사", false));

		assertThatThrownBy(() -> addressService.update(1L, office.id(), createRequest("회사주소수정", " 집 ", null)))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException)ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_DUPLICATE_LABEL);
	}

	@Test
	@DisplayName("수정 시 자기 자신의 라벨과 동일한 값은 허용된다")
	void updateLabelAllowsSameLabelOfSameAddress() {
		AddressDto.Response created = addressService.create(1L, createRequest("집주소", "HOME", false));

		AddressDto.Response updated = addressService.update(1L, created.id(), createRequest("집주소수정", " home ", null));

		assertThat(updated.label()).isEqualTo("home");
	}

	private AddressDto.SaveRequest createRequest(String receiverName, String label, Boolean isDefault) {
		return new AddressDto.SaveRequest(
			receiverName,
			label,
			"010-1111-2222",
			"12345",
			"서울",
			"101동",
			"문앞",
			isDefault
		);
	}
}
