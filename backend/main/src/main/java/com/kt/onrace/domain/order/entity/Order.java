package com.kt.onrace.domain.order.entity;

import java.util.ArrayList;
import java.util.List;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

	@Column(unique = true, nullable = false)
	private String orderNumber;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private Long eventCourseId;

	private Long eventPaceId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus orderStatus;

	@Column(nullable = false)
	private Long itemTotalAmount;

	@Column(nullable = false)
	private Long shippingFee;

	@Column(nullable = false)
	private Long discountAmount;

	@Column(nullable = false)
	private Long finalAmount;

	@Column(nullable = false, length = 50)
	private String recipientName;

	@Column(nullable = false, length = 20)
	private String recipientPhone;

	@Column(nullable = false, length = 10)
	private String zipCode;

	@Column(nullable = false, length = 200)
	private String address;

	@Column(length = 200)
	private String detailAddress;

	@Column(length = 200)
	private String deliveryMemo;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<OrderPackage> packages = new ArrayList<>();

	@Builder
	public Order(String orderNumber, Long userId, Long eventCourseId, Long eventPaceId, OrderStatus orderStatus,
		Long itemTotalAmount, Long shippingFee, Long discountAmount, Long finalAmount,
		String recipientName, String recipientPhone, String zipCode, String address, String detailAddress,
		String deliveryMemo) {
		this.orderNumber = orderNumber;
		this.userId = userId;
		this.eventCourseId = eventCourseId;
		this.eventPaceId = eventPaceId;
		this.orderStatus = orderStatus;
		this.itemTotalAmount = itemTotalAmount;
		this.shippingFee = shippingFee;
		this.discountAmount = discountAmount;
		this.finalAmount = finalAmount;
		this.recipientName = recipientName;
		this.recipientPhone = recipientPhone;
		this.zipCode = zipCode;
		this.address = address;
		this.detailAddress = detailAddress;
		this.deliveryMemo = deliveryMemo;
	}

	public void addPackage(OrderPackage orderPackage) {
		this.packages.add(orderPackage);
		orderPackage.setOrder(this);
	}

}
