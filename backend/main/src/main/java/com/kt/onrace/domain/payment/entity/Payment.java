package com.kt.onrace.domain.payment.entity;

import com.kt.onrace.common.entity.BaseEntity;
import com.kt.onrace.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

	@Column(length = 200)
	private String paymentKey;

	@Column(nullable = false, length = 100)
	private String orderNumber;

	@Column(nullable = false)
	private Long amount;

	@Column(length = 50)
	private String method;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	public Payment(String paymentKey, String orderNumber, Long amount, String method, PaymentStatus status, Member member) {
		this.paymentKey = paymentKey;
		this.orderNumber = orderNumber;
		this.amount = amount;
		this.method = method;
		this.status = status;
		this.member = member;
	}

	public void updateStatus(PaymentStatus status) {
		this.status = status;
	}

}
