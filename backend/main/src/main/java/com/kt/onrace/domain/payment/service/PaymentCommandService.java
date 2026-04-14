package com.kt.onrace.domain.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.domain.member.entity.Member;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.service.OrderService;
import com.kt.onrace.domain.payment.dto.TossPaymentResponseDto;
import com.kt.onrace.domain.payment.entity.Payment;
import com.kt.onrace.domain.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

	private final PaymentRepository paymentRepository;
	private final OrderService orderService;

	@Transactional
	public void savePaymentAndConfirmOrder(Member member, Order order, TossPaymentResponseDto responseDto, Long userId) {
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
	}
}
