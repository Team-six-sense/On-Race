package com.kt.onrace.domain.address;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.address.dto.AddressDto;
import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;

@ExtendWith(MockitoExtension.class)
class AddressServiceExceptionTranslationTest {

	private static final long USER_ID = 1L;

	@InjectMocks
	private com.kt.onrace.domain.address.service.AddressService addressService;

	@Mock
	private AddressRepository addressRepository;

	@Test
	@DisplayName("생성 시 normalized label 유니크 제약 위반은 중복 라벨 예외로 변환된다")
	void createTranslatesNormalizedLabelConstraintViolation() {
		AddressDto.SaveRequest request = createRequest("집");
		DataIntegrityViolationException exception = duplicateLabelViolation("PUBLIC.UK_ADDRESS_USER_NORMALIZED_LABEL_INDEX_E");

		given(addressRepository.existsByUserIdAndNormalizedLabel(anyLong(), anyString())).willReturn(false);
		given(addressRepository.existsByUserId(USER_ID)).willReturn(false);
		given(addressRepository.saveAndFlush(any(Address.class))).willThrow(exception);

		assertThatThrownBy(() -> addressService.create(USER_ID, request))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException) ex).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_DUPLICATE_LABEL);
	}

	@Test
	@DisplayName("생성 시 다른 제약 위반은 중복 라벨 예외로 변환하지 않는다")
	void createDoesNotTranslateOtherConstraintViolation() {
		AddressDto.SaveRequest request = createRequest("집");
		DataIntegrityViolationException exception = duplicateLabelViolation("uk_other_constraint");

		given(addressRepository.existsByUserIdAndNormalizedLabel(anyLong(), anyString())).willReturn(false);
		given(addressRepository.existsByUserId(USER_ID)).willReturn(false);
		given(addressRepository.saveAndFlush(any(Address.class))).willThrow(exception);

		assertThatThrownBy(() -> addressService.create(USER_ID, request))
			.isSameAs(exception);
	}

	private AddressDto.SaveRequest createRequest(String label) {
		return new AddressDto.SaveRequest(
			"홍길동",
			label,
			"010-1111-2222",
			"12345",
			"서울",
			"101동",
			"문앞",
			false
		);
	}

	private DataIntegrityViolationException duplicateLabelViolation(String constraintName) {
		ConstraintViolationException cause = new ConstraintViolationException(
			"duplicate label",
			new SQLException("duplicate entry"),
			"insert into address ...",
			constraintName
		);
		return new DataIntegrityViolationException("could not execute statement", cause);
	}
}
