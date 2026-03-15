package com.kt.onrace.domain.order.service;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventImage;
import com.kt.onrace.domain.event.entity.EventImageType;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.event.entity.EventPackage;
import com.kt.onrace.domain.event.repository.EventCourseRepository;
import com.kt.onrace.domain.event.repository.EventPaceRepository;
import com.kt.onrace.domain.event.repository.EventPackageRepository;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.order.dto.CheckoutPrepareRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutPrepareResponseDto;
import com.kt.onrace.domain.order.dto.CheckoutRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutResponseDto;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderPackage;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.kt.onrace.domain.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final EventRepository eventRepository;
	private final EventCourseRepository eventCourseRepository;
	private final EventPaceRepository eventPaceRepository;
	private final EventPackageRepository eventPackageRepository;
	private final AddressRepository addressRepository;
	private final OrderRepository orderRepository;

	@Transactional(readOnly = true)
	public CheckoutPrepareResponseDto getCheckoutPrepareInfo(CheckoutPrepareRequestDto request, Long userId) {

		Event event = eventRepository.findByIdOrThrow(request.eventId(), BusinessErrorCode.EVENT_NOT_FOUND);

		EventCourse course = eventCourseRepository.findByIdAndEventIdOrThrow(
			request.eventCourseId(), request.eventId(), BusinessErrorCode.COMMON_INVALID_FORMAT);

		EventPace pace = eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			request.eventPaceId(), request.eventCourseId(), BusinessErrorCode.COMMON_INVALID_FORMAT);

		List<EventPackage> packages = eventPackageRepository.findByEventId(event.getId());

		Long itemTotalAmount = course.getPrice();
		// 임시 배송비, 배송지에 따라 달라질 수 있음
		Long shippingFee = 3000L;
		// 임시 할인비, 할인 정책에 따라 달라질 수 있음
		long discountAmount = 0L;

		Long finalAmount = itemTotalAmount + shippingFee - discountAmount;

		CheckoutPrepareResponseDto.PaymentDetail paymentDetail = new CheckoutPrepareResponseDto.PaymentDetail(
			itemTotalAmount, shippingFee, discountAmount, finalAmount);

		String prepareToken = UUID.randomUUID().toString();
		CheckoutPrepareResponseDto.ShippingAddressInfo shippingAddress = resolveShippingAddress(userId, request.addressId());
		String thumbnailUrl = extractThumbnailUrl(event);

		return CheckoutPrepareResponseDto.of(
			prepareToken,
			event,
			course,
			pace,
			packages,
			paymentDetail,
			thumbnailUrl,
			shippingAddress
		);
	}

	@Transactional
	public CheckoutResponseDto checkout(CheckoutRequestDto request, Long userId) {
		Event event = eventRepository.findByIdOrThrow(request.eventId(), BusinessErrorCode.EVENT_NOT_FOUND);

		EventCourse course = eventCourseRepository.findByIdAndEventIdOrThrow(
			request.eventCourseId(), request.eventId(), BusinessErrorCode.COMMON_INVALID_FORMAT);

		EventPace pace = eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			request.eventPaceId(), request.eventCourseId(), BusinessErrorCode.COMMON_INVALID_FORMAT);

		List<EventPackage> selectedPackages = resolveSelectedPackages(request.eventId(), request.selectedPackageIds());
		ShippingAddressSnapshot shippingAddress = resolveShippingAddressSnapshot(userId, request);

		Long itemTotalAmount = course.getPrice() + selectedPackages.stream().mapToLong(EventPackage::getPrice).sum();
		// 임시 배송비, 배송지에 따라 달라질 수 있음
		Long shippingFee = 3000L;
		// 임시 할인비, 할인 정책에 따라 달라질 수 있음
		long discountAmount = 0L;

		Long calculatedFinalAmount = itemTotalAmount + shippingFee - discountAmount;

		if (!calculatedFinalAmount.equals(request.expectedFinalAmount())) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_FORMAT);
		}

		String orderNumber = "ORD-" + System.currentTimeMillis() + "-"
			+ UUID.randomUUID().toString().substring(0, 4).toUpperCase();

		Order order = Order.builder()
			.orderNumber(orderNumber)
			.userId(userId)
			.eventCourseId(course.getId())
			.eventPaceId(pace.getId())
			.orderStatus(OrderStatus.PENDING)
			.itemTotalAmount(itemTotalAmount)
			.shippingFee(shippingFee)
			.discountAmount(discountAmount)
			.finalAmount(calculatedFinalAmount)
			.recipientName(shippingAddress.receiverName())
			.recipientPhone(shippingAddress.phone())
			.zipCode(shippingAddress.zipcode())
			.address(shippingAddress.address1())
			.detailAddress(shippingAddress.address2())
			.deliveryMemo(shippingAddress.memo())
			.build();

		for (EventPackage pkg : selectedPackages) {
			OrderPackage orderPackage = OrderPackage.builder()
				.eventPackageId(pkg.getId())
				.price(pkg.getPrice())
				.name(pkg.getName())
				.build();
			order.addPackage(orderPackage);
		}

		orderRepository.save(order);

		String orderName = event.getTitle() + " " + course.getName();
		if (!selectedPackages.isEmpty()) {
			orderName += " 외 " + selectedPackages.size() + "건";
		}

		return new CheckoutResponseDto(order.getOrderNumber(), orderName, order.getFinalAmount());
	}

	private List<EventPackage> resolveSelectedPackages(Long eventId, List<Long> selectedPackageIds) {
		if (selectedPackageIds == null || selectedPackageIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<EventPackage> selectedPackages = eventPackageRepository.findAllById(selectedPackageIds);
		boolean hasInvalidPackage = selectedPackages.size() != selectedPackageIds.size()
			|| selectedPackages.stream().anyMatch(pkg -> !pkg.getEvent().getId().equals(eventId));

		if (hasInvalidPackage) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_FORMAT);
		}

		return selectedPackages;
	}

	private CheckoutPrepareResponseDto.ShippingAddressInfo resolveShippingAddress(Long userId, Long addressId) {
		return findAddressForCheckout(userId, addressId)
			.map(address -> new CheckoutPrepareResponseDto.ShippingAddressInfo(
				true,
				address.getId(),
				address.getReceiverName(),
				address.getPhone(),
				address.getZipcode(),
				address.getAddress1(),
				address.getAddress2(),
				address.getMemo(),
				address.isDefault()
			))
			.orElseGet(CheckoutPrepareResponseDto.ShippingAddressInfo::empty);
	}

	private ShippingAddressSnapshot resolveShippingAddressSnapshot(Long userId, CheckoutRequestDto request) {
		if (request.addressId() != null) {
			Address address = addressRepository.findByIdAndUserId(request.addressId(), userId)
				.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));

			return new ShippingAddressSnapshot(
				address.getReceiverName(),
				address.getPhone(),
				address.getZipcode(),
				address.getAddress1(),
				address.getAddress2(),
				request.deliveryMemo() != null ? request.deliveryMemo() : address.getMemo()
			);
		}

		if (isBlank(request.recipientName()) || isBlank(request.recipientPhone()) || isBlank(request.zipCode())
			|| isBlank(request.address())) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_FORMAT);
		}

		return new ShippingAddressSnapshot(
			request.recipientName(),
			request.recipientPhone(),
			request.zipCode(),
			request.address(),
			request.detailAddress(),
			request.deliveryMemo()
		);
	}

	private java.util.Optional<Address> findAddressForCheckout(Long userId, Long addressId) {
		if (addressId != null) {
			return addressRepository.findByIdAndUserId(addressId, userId);
		}

		return addressRepository.findFirstByUserIdAndIsDefaultTrue(userId)
			.or(() -> addressRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().findFirst());
	}

	private String extractThumbnailUrl(Event event) {
		return event.getImages().stream()
			.filter(image -> image.getType() == EventImageType.THUMBNAIL)
			.sorted(Comparator.comparingInt(EventImage::getSort))
			.map(EventImage::getUrl)
			.findFirst()
			.orElse(null);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private record ShippingAddressSnapshot(
		String receiverName,
		String phone,
		String zipcode,
		String address1,
		String address2,
		String memo
	) {
	}

}
