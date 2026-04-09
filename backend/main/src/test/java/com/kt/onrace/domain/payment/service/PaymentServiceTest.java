package com.kt.onrace.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.member.entity.Member;
import com.kt.onrace.domain.member.repository.MemberRepository;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.kt.onrace.domain.order.repository.OrderRepository;
import com.kt.onrace.domain.order.service.OrderService;
import com.kt.onrace.domain.payment.client.TossPaymentClient;
import com.kt.onrace.domain.payment.dto.PaymentConfirmRequestDto;
import com.kt.onrace.domain.payment.dto.TossPaymentResponseDto;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private TossPaymentClient tossPaymentClient;

	@Mock
	private OrderService orderService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PaymentCommandService paymentCommandService;

	@InjectMocks
	private PaymentService paymentService;

	@Test
	@DisplayName("Toss API 호출 실패 시 failPayment만 호출되고 그 이후 로직은 실행되지 않는다")
	void confirmPayment_failTossApi_executesFailPaymentAndThrows() {
		// given
		Long userId = 1L;
		PaymentConfirmRequestDto requestDto = new PaymentConfirmRequestDto("payment-key", "ORD-123", 50000L);

		Member member = mock(Member.class);
		Order order = createPendingOrder("ORD-123", userId, 50000L);

		when(memberRepository.findByIdAndIsDeletedFalseOrThrow(eq(userId), eq(BusinessErrorCode.MEMBER_NOT_FOUND)))
			.thenReturn(member);
		when(orderRepository.findByOrderNumberAndUserId(eq("ORD-123"), eq(userId)))
			.thenReturn(Optional.of(order));

		// Toss API 예외 발생 시뮬레이션
		BusinessException apiException = new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
		when(tossPaymentClient.confirmPayment(requestDto)).thenThrow(apiException);

		// when & then
		assertThatThrownBy(() -> paymentService.confirmPayment(userId, requestDto))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(BusinessErrorCode.COMMON_SYSTEM_ERROR.getMessage());

		// 1. failPayment가 정상적으로 호출되었는지 검증
		verify(orderService).failPayment("ORD-123", userId);

		// 2. 뒤이은 savePaymentAndConfirmOrder는 호출되지 않았는지 시각적으로 검증
		verify(paymentCommandService, never()).savePaymentAndConfirmOrder(
			any(), any(), any(), any()
		);
	}

	@Test
	@DisplayName("단순히 금액이 불일치하는 경우, Toss API를 아예 호출하지 않고 즉시 예외를 던진다 (뒤쪽 코드 실행안됨)")
	void confirmPayment_amountMismatch_throwsImmediately() {
		// given
		Long userId = 1L;
		PaymentConfirmRequestDto requestDto = new PaymentConfirmRequestDto("payment-key", "ORD-123", 10000L); // 금액 다름

		Member member = mock(Member.class);
		Order order = createPendingOrder("ORD-123", userId, 50000L); // DB금액 = 50000

		when(memberRepository.findByIdAndIsDeletedFalseOrThrow(eq(userId), eq(BusinessErrorCode.MEMBER_NOT_FOUND)))
			.thenReturn(member);
		when(orderRepository.findByOrderNumberAndUserId(eq("ORD-123"), eq(userId)))
			.thenReturn(Optional.of(order));

		// when & then
		assertThatThrownBy(() -> paymentService.confirmPayment(userId, requestDto))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(BusinessErrorCode.PAYMENT_AMOUNT_MISMATCH);

		// failPayment가 즉시 호출되어 DB변경
		verify(orderService).failPayment("ORD-123", userId);

		// Toss API 및 남은 저장 로직이 아무것도 실행되지 않음
		verify(tossPaymentClient, never()).confirmPayment(any());
		verify(paymentCommandService, never()).savePaymentAndConfirmOrder(any(), any(), any(), any());
	}

	@Test
	@DisplayName("Toss API 성공 시 예외를 던지지 않고 마지막까지 실행된다")
	void confirmPayment_success_executesTillEnd() {
		// given
		Long userId = 1L;
		PaymentConfirmRequestDto requestDto = new PaymentConfirmRequestDto("payment-key", "ORD-123", 50000L);

		Member member = mock(Member.class);
		Order order = createPendingOrder("ORD-123", userId, 50000L);
		TossPaymentResponseDto responseDto = mock(TossPaymentResponseDto.class);

		when(memberRepository.findByIdAndIsDeletedFalseOrThrow(eq(userId), eq(BusinessErrorCode.MEMBER_NOT_FOUND)))
			.thenReturn(member);
		when(orderRepository.findByOrderNumberAndUserId(eq("ORD-123"), eq(userId)))
			.thenReturn(Optional.of(order));
		when(tossPaymentClient.confirmPayment(requestDto)).thenReturn(responseDto);

		// when
		TossPaymentResponseDto result = paymentService.confirmPayment(userId, requestDto);

		// then
		verify(orderService, never()).failPayment(any(), any());
		verify(paymentCommandService).savePaymentAndConfirmOrder(member, order, responseDto, userId);
	}

	private Order createPendingOrder(String orderNumber, Long userId, Long finalAmount) {
		return Order.builder()
			.orderNumber(orderNumber)
			.userId(userId)
			.orderStatus(OrderStatus.PENDING)
			.finalAmount(finalAmount)
			.build();
	}

	private <T> T setField(T target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
			return target;
		} catch (Exception e) {
			try {
				Field superField = target.getClass().getSuperclass().getDeclaredField(fieldName);
				superField.setAccessible(true);
				superField.set(target, value);
				return target;
			} catch (Exception ex) {
				throw new RuntimeException("Could not set field " + fieldName, ex);
			}
		}
	}
}
