package com.kt.onrace.domain.address;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kt.onrace.domain.address.dto.AddressDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class AddressDtoValidationTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	@DisplayName("라벨이 20자면 검증을 통과한다")
	void saveRequestAcceptsLabelWith20Characters() {
		Set<ConstraintViolation<AddressDto.SaveRequest>> violations = validator.validate(
			createRequest("abcdefghijklmnopqrst")
		);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("라벨이 20자를 초과하면 검증에 실패한다")
	void saveRequestRejectsLabelLongerThan20Characters() {
		Set<ConstraintViolation<AddressDto.SaveRequest>> violations = validator.validate(
			createRequest("가나다라마바사아자차카타파하가나다라마바사")
		);

		assertThat(violations)
			.extracting(ConstraintViolation::getMessage)
			.contains("주소 별칭은 20자를 초과할 수 없습니다.");
	}

	@Test
	@DisplayName("라벨에 허용되지 않은 문자가 포함되면 검증에 실패한다")
	void saveRequestRejectsLabelWithInvalidCharacters() {
		Set<ConstraintViolation<AddressDto.SaveRequest>> violations = validator.validate(
			createRequest("집!")
		);

		assertThat(violations)
			.extracting(ConstraintViolation::getMessage)
			.contains("주소 별칭에는 한글, 영문, 숫자, 공백만 사용할 수 있습니다.");
	}

	@Test
	@DisplayName("전화번호는 숫자와 하이픈만 포함하면 검증을 통과한다")
	void saveRequestAcceptsPhoneWithDigitsAndHyphen() {
		Set<ConstraintViolation<AddressDto.SaveRequest>> violations = validator.validate(
			createRequest("집", "010-1111-2222")
		);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("전화번호에 숫자와 하이픈 외 문자가 포함되면 검증에 실패한다")
	void saveRequestRejectsPhoneWithInvalidCharacters() {
		Set<ConstraintViolation<AddressDto.SaveRequest>> violations = validator.validate(
			createRequest("집", "010-1111-222A")
		);

		assertThat(violations)
			.extracting(ConstraintViolation::getMessage)
			.contains("전화번호는 숫자와 하이픈만 입력할 수 있습니다.");
	}

	private AddressDto.SaveRequest createRequest(String label) {
		return createRequest(label, "010-1111-2222");
	}

	private AddressDto.SaveRequest createRequest(String label, String phone) {
		return new AddressDto.SaveRequest(
			"홍길동",
			label,
			phone,
			"12345",
			"서울",
			"101동",
			"문앞",
			false
		);
	}
}
