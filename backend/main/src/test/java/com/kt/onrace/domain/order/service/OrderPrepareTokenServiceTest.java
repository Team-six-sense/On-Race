package com.kt.onrace.domain.order.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.order.config.OrderPrepareTokenProperties;
import com.kt.onrace.domain.order.dto.CheckoutRequestDto;

class OrderPrepareTokenServiceTest {

	@Test
	@DisplayName("prepare token은 동일한 user/event/course/pace 요청에 대해서만 검증된다")
	void validatePrepareTokenWithMatchingPayload() {
		OrderPrepareTokenService service = createService(300L);
		String token = service.issueToken(7L, 1L, 10L, 20L);

		OrderPrepareTokenService.ValidatedPrepareToken validated = service.validatePrepareToken(
			token,
			7L,
			checkoutRequest(token, 1L, 10L, 20L)
		);

		service.consumePrepareToken(validated);
	}

	@Test
	@DisplayName("prepare token은 다른 유저로 checkout하면 차단된다")
	void rejectPrepareTokenWhenUserDoesNotMatch() {
		OrderPrepareTokenService service = createService(300L);
		String token = service.issueToken(7L, 1L, 10L, 20L);

		assertThatThrownBy(() -> service.validatePrepareToken(
			token,
			8L,
			checkoutRequest(token, 1L, 10L, 20L)
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.COMMON_INVALID_FORMAT);
	}

	@Test
	@DisplayName("prepare token은 event/course/pace가 바뀌면 차단된다")
	void rejectPrepareTokenWhenPayloadIsTampered() {
		OrderPrepareTokenService service = createService(300L);
		String token = service.issueToken(7L, 1L, 10L, 20L);

		assertThatThrownBy(() -> service.validatePrepareToken(
			token,
			7L,
			checkoutRequest(token, 1L, 10L, 21L)
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.COMMON_INVALID_FORMAT);
	}

	@Test
	@DisplayName("prepare token은 한 번 consume되면 재사용할 수 없다")
	void rejectPrepareTokenReuse() {
		OrderPrepareTokenService service = createService(300L);
		String token = service.issueToken(7L, 1L, 10L, 20L);
		OrderPrepareTokenService.ValidatedPrepareToken validated = service.validatePrepareToken(
			token,
			7L,
			checkoutRequest(token, 1L, 10L, 20L)
		);

		service.consumePrepareToken(validated);

		assertThatThrownBy(() -> service.consumePrepareToken(validated))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.COMMON_DUPLICATE_REQUEST);
	}

	@Test
	@DisplayName("prepare token은 만료되면 검증할 수 없다")
	void rejectExpiredPrepareToken() {
		OrderPrepareTokenService service = createService(-1L);
		String token = service.issueToken(7L, 1L, 10L, 20L);

		assertThatThrownBy(() -> service.validatePrepareToken(
			token,
			7L,
			checkoutRequest(token, 1L, 10L, 20L)
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.COMMON_INVALID_REQUEST);
	}

	private OrderPrepareTokenService createService(long ttlSeconds) {
		OrderPrepareTokenProperties properties = new OrderPrepareTokenProperties();
		properties.setTtlSeconds(ttlSeconds);

		return new OrderPrepareTokenService(
			properties,
			new InMemoryOrderPrepareTokenNonceStore(),
			"test-secret-key-1234567890-1234567890-1234567890"
		);
	}

	private CheckoutRequestDto checkoutRequest(String token, Long eventId, Long eventCourseId, Long eventPaceId) {
		return new CheckoutRequestDto(
			token,
			eventId,
			eventCourseId,
			eventPaceId,
			java.util.List.of(),
			53000L,
			null,
			null,
			null,
			null,
			null,
			null,
			null
		);
	}
}
