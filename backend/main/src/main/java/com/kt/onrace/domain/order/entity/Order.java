package com.kt.onrace.domain.order.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.kt.onrace.common.entity.BaseEntity;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventStatus;

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

	@Column
	private Long entryId;

	@Column
	private Long eventId;

	@Column(length = 50)
	private String eventTitle;

	@Enumerated(EnumType.STRING)
	@Column
	private EventAppType eventAppType;

	@Enumerated(EnumType.STRING)
	@Column
	private EventStatus eventStatus;

	@Column
	private LocalDateTime eventAt;

	@Column(length = 255)
	private String eventVenue;

	@Column(length = 50)
	private String courseName;

	@Column(length = 50)
	private String paceName;

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

	@Column(length = 20)
	private String addressLabel;

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

	public static Order createPending(
		String orderNumber,
		Long userId,
		Long entryId,
		OrderSelection selection,
		OrderEventSnapshot eventSnapshot,
		PaymentSnapshot paymentSnapshot,
		RecipientSnapshot recipientSnapshot
	) {
		return Order.builder()
			.orderNumber(orderNumber)
			.userId(userId)
			.eventCourseId(selection.eventCourseId())
			.eventPaceId(selection.eventPaceId())
			.entryId(entryId)
			.eventId(eventSnapshot != null ? eventSnapshot.eventId() : null)
			.eventTitle(eventSnapshot != null ? eventSnapshot.eventTitle() : null)
			.eventAppType(eventSnapshot != null ? eventSnapshot.eventAppType() : null)
			.eventStatus(eventSnapshot != null ? eventSnapshot.eventStatus() : null)
			.eventAt(eventSnapshot != null ? eventSnapshot.eventAt() : null)
			.eventVenue(eventSnapshot != null ? eventSnapshot.eventVenue() : null)
			.courseName(eventSnapshot != null ? eventSnapshot.courseName() : null)
			.paceName(eventSnapshot != null ? eventSnapshot.paceName() : null)
			.orderStatus(OrderStatus.PENDING)
			.itemTotalAmount(paymentSnapshot.itemTotalAmount())
			.shippingFee(paymentSnapshot.shippingFee())
			.discountAmount(paymentSnapshot.discountAmount())
			.finalAmount(paymentSnapshot.finalAmount())
			.recipientName(recipientSnapshot.recipientName())
			.addressLabel(recipientSnapshot.addressLabel())
			.recipientPhone(recipientSnapshot.recipientPhone())
			.zipCode(recipientSnapshot.zipCode())
			.address(recipientSnapshot.address())
			.detailAddress(recipientSnapshot.detailAddress())
			.deliveryMemo(recipientSnapshot.deliveryMemo())
			.build();
	}

	@Builder
	public Order(String orderNumber, Long userId, Long eventCourseId, Long eventPaceId, Long entryId,
		Long eventId, String eventTitle, EventAppType eventAppType, EventStatus eventStatus, LocalDateTime eventAt,
		String eventVenue, String courseName, String paceName, OrderStatus orderStatus,
		Long itemTotalAmount, Long shippingFee, Long discountAmount, Long finalAmount,
		String recipientName, String addressLabel, String recipientPhone, String zipCode, String address, String detailAddress,
		String deliveryMemo) {
		this.orderNumber = orderNumber;
		this.userId = userId;
		this.eventCourseId = eventCourseId;
		this.eventPaceId = eventPaceId;
		this.entryId = entryId;
		this.eventId = eventId;
		this.eventTitle = eventTitle;
		this.eventAppType = eventAppType;
		this.eventStatus = eventStatus;
		this.eventAt = eventAt;
		this.eventVenue = eventVenue;
		this.courseName = courseName;
		this.paceName = paceName;
		this.orderStatus = orderStatus;
		this.itemTotalAmount = itemTotalAmount;
		this.shippingFee = shippingFee;
		this.discountAmount = discountAmount;
		this.finalAmount = finalAmount;
		this.recipientName = recipientName;
		this.addressLabel = addressLabel;
		this.recipientPhone = recipientPhone;
		this.zipCode = zipCode;
		this.address = address;
		this.detailAddress = detailAddress;
		this.deliveryMemo = deliveryMemo;
	}

	public record OrderEventSnapshot(
		Long eventId,
		String eventTitle,
		EventAppType eventAppType,
		EventStatus eventStatus,
		LocalDateTime eventAt,
		String eventVenue,
		String courseName,
		String paceName
	) {
	}

	public record OrderSelection(
		Long eventCourseId,
		Long eventPaceId
	) {
	}

	public record PaymentSnapshot(
		Long itemTotalAmount,
		Long shippingFee,
		Long discountAmount,
		Long finalAmount
	) {
	}

	public record RecipientSnapshot(
		String recipientName,
		String addressLabel,
		String recipientPhone,
		String zipCode,
		String address,
		String detailAddress,
		String deliveryMemo
	) {
	}

	public void addPackage(OrderPackage orderPackage) {
		this.packages.add(orderPackage);
		orderPackage.setOrder(this);
	}

	public void applyEventSnapshot(OrderEventSnapshot eventSnapshot) {
		if (eventSnapshot == null) {
			return;
		}

		this.eventId = eventSnapshot.eventId();
		this.eventTitle = eventSnapshot.eventTitle();
		this.eventAppType = eventSnapshot.eventAppType();
		this.eventStatus = eventSnapshot.eventStatus();
		this.eventAt = eventSnapshot.eventAt();
		this.eventVenue = eventSnapshot.eventVenue();
		this.courseName = eventSnapshot.courseName();
		this.paceName = eventSnapshot.paceName();
	}

	public void markPaid() {
		transitionFromPending(OrderStatus.PAID);
	}

	public void markCancelled() {
		transitionFromPending(OrderStatus.CANCELLED);
	}

	public void markExpired() {
		transitionFromPending(OrderStatus.EXPIRED);
	}

	public void markFailed() {
		transitionFromPending(OrderStatus.FAILED);
	}

	private void transitionFromPending(OrderStatus targetStatus) {
		if (this.orderStatus == targetStatus) {
			return;
		}

		if (this.orderStatus != OrderStatus.PENDING) {
			throw new BusinessException(BusinessErrorCode.ORDER_CANNOT_CONFIRM);
		}

		this.orderStatus = targetStatus;
	}

}
