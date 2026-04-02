package com.kt.onrace.domain.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.kt.onrace.domain.payment.entity.Payment;
import com.kt.onrace.domain.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final TossPaymentClient tossPaymentClient;
	private final OrderService orderService;
	private final MemberRepository memberRepository;

	@Transactional
	public TossPaymentResponseDto confirmPayment(Long userId, PaymentConfirmRequestDto requestDto) {
		Member member = memberRepository.findByIdAndIsDeletedFalseOrThrow(userId, BusinessErrorCode.MEMBER_NOT_FOUND);

		Order order = orderRepository.findByOrderNumberAndUserId(requestDto.orderId(), userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ORDER_NOT_FOUND));

		if (order.getOrderStatus() != OrderStatus.PENDING) {
			throw new BusinessException(BusinessErrorCode.ORDER_CANNOT_CONFIRM);
		}

		if (!order.getFinalAmount().equals(requestDto.amount())) {
			orderService.failPayment(order.getOrderNumber(), userId);
			throw new BusinessException(BusinessErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}

		TossPaymentResponseDto responseDto;
		try {
			responseDto = tossPaymentClient.confirmPayment(requestDto);
		} catch (BusinessException e) {
			orderService.failPayment(order.getOrderNumber(), userId);
			throw e;
		}

		Payment payment = Payment.builder()
			.paymentKey(responseDto.paymentKey())
			.orderNumber(responseDto.orderId())
			.amount(responseDto.totalAmount())
			.method(responseDto.method())
			.status(responseDto.status())
			.member(member)
			.build();
			
		paymentRepository.save(payment);
		orderService.confirmPayment(order.getOrderNumber(), userId);

		return responseDto;
	}
}
