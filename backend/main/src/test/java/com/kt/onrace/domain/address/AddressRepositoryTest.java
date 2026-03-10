package com.kt.onrace.domain.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.kt.onrace.common.config.JpaAuditingConfig;
import com.kt.onrace.common.config.QueryDslConfig;
import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressLabelProjection;
import com.kt.onrace.domain.address.repository.AddressRepository;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
class AddressRepositoryTest {

	@Autowired
	private AddressRepository addressRepository;

	@Test
	@DisplayName("라벨 projection 조회는 요청한 사용자의 id와 label만 반환한다")
	void findLabelProjectionsByUserIdReturnsLabelsForRequestedUser() {
		Address home = addressRepository.save(createAddress(1L, "집"));
		Address office = addressRepository.save(createAddress(1L, "회사"));
		addressRepository.save(createAddress(2L, "다른유저"));

		List<AddressLabelProjection> projections = addressRepository.findLabelProjectionsByUserId(1L);

		assertThat(projections)
			.extracting(AddressLabelProjection::getId, AddressLabelProjection::getLabel)
			.containsOnly(
				tuple(home.getId(), "집"),
				tuple(office.getId(), "회사")
			);
	}

	@Test
	@DisplayName("정규화된 라벨 유니크 제약은 trim 및 대소문자 무시 기준으로 중복 저장을 막는다")
	void uniqueConstraintBlocksDuplicateNormalizedLabel() {
		addressRepository.saveAndFlush(createAddress(1L, "HOME"));

		assertThatThrownBy(() -> addressRepository.saveAndFlush(createAddress(1L, " home ")))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Address createAddress(Long userId, String label) {
		return Address.builder()
			.userId(userId)
			.receiverName("홍길동")
			.label(label)
			.phone("010-1111-2222")
			.zipcode("12345")
			.address1("서울")
			.address2("101동")
			.memo("문앞")
			.isDefault(false)
			.build();
	}
}
