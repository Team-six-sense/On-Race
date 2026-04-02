package com.kt.onrace.domain.order.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.kt.onrace.domain.order.contract.OrderCheckoutEligibility;
import com.kt.onrace.domain.order.contract.OrderEntryContract;
import com.kt.onrace.domain.order.dto.CheckoutPrepareRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutPrepareResponseDto;
import com.kt.onrace.domain.order.dto.CheckoutRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutResponseDto;
import com.kt.onrace.domain.order.dto.OrderDetailResponseDto;
import com.kt.onrace.domain.order.dto.OrderListResponseDto;
import com.kt.onrace.domain.order.dto.OrderSummaryDto;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderPackage;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.kt.onrace.domain.order.repository.OrderPackageRepository;
import com.kt.onrace.domain.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Order는 결제 화면에 필요한 checkout-info를 조합하고,
 * 실제 주문 생성 시 배송지/금액/옵션을 스냅샷으로 저장하는 역할을 한다.
 */
public class OrderService {

	private final EventRepository eventRepository;
	private final EventCourseRepository eventCourseRepository;
	private final EventPaceRepository eventPaceRepository;
	private final EventPackageRepository eventPackageRepository;
	private final AddressRepository addressRepository;
	private final OrderRepository orderRepository;
	private final OrderPackageRepository orderPackageRepository;
	private final OrderEntryContract orderEntryContract;
	private final OrderPrepareTokenService orderPrepareTokenService;

	@Transactional(readOnly = true)
	public OrderListResponseDto getOrders(String tab, Long userId) {
		OrderStatus orderStatus = resolveOrderStatus(tab);
		List<Order> orders = orderRepository.findByUserIdAndOrderStatusOrderByCreatedAtDesc(userId, orderStatus);

		return new OrderListResponseDto(buildOrderSummaries(orders));
	}

	@Transactional(readOnly = true)
	public OrderDetailResponseDto getOrderDetail(String orderNumber, Long userId) {
		Order order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ORDER_NOT_FOUND));

		Map<Long, EventCourse> courseById = loadCourses(List.of(order));
		Map<Long, EventPace> paceById = loadPaces(List.of(order));
		Map<Long, Event> eventById = loadEvents(courseById.values());
		Map<Long, String> thumbnailByEventId = buildThumbnailMap(eventById);

		EventCourse course = courseById.get(order.getEventCourseId());
		Long eventId = course != null && course.getEvent() != null ? course.getEvent().getId() : null;
		Event event = eventId != null ? eventById.get(eventId) : null;
		EventPace pace = order.getEventPaceId() != null ? paceById.get(order.getEventPaceId()) : null;

		List<OrderDetailResponseDto.PackageInfo> packages = orderPackageRepository.findByOrderIdOrderByIdAsc(
				order.getId())
			.stream()
			.map(orderPackage -> new OrderDetailResponseDto.PackageInfo(
				orderPackage.getEventPackageId(),
				orderPackage.getName(),
				orderPackage.getPrice()
			))
			.toList();

		return new OrderDetailResponseDto(
			eventId,
			order.getOrderNumber(),
			order.getOrderStatus(),
			order.getCreatedAt(),
			order.getItemTotalAmount(),
			order.getShippingFee(),
			order.getDiscountAmount(),
			order.getFinalAmount(),
			event != null ? event.getTitle() : null,
			eventId != null ? thumbnailByEventId.get(eventId) : null,
			course != null ? course.getName() : null,
			pace != null ? pace.getName() : null,
			order.getRecipientName(),
			order.getAddressLabel(),
			order.getRecipientPhone(),
			order.getZipCode(),
			order.getAddress(),
			order.getDetailAddress(),
			order.getDeliveryMemo(),
			packages
		);
	}

	@Transactional
	public void confirmPayment(String orderNumber, Long userId) {
		Order order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ORDER_NOT_FOUND));

		boolean transitionedFromPending = order.getOrderStatus() == OrderStatus.PENDING;
		order.markPaid();
		runPaymentConfirmedFollowUp(order, transitionedFromPending);
	}

	@Transactional
	public void failPayment(String orderNumber, Long userId) {
		transitionPendingOrderToTerminal(orderNumber, userId, Order::markFailed);
	}

	@Transactional
	public void cancelPayment(String orderNumber, Long userId) {
		transitionPendingOrderToTerminal(orderNumber, userId, Order::markCancelled);
	}

	@Transactional
	public void expirePayment(String orderNumber, Long userId) {
		transitionPendingOrderToTerminal(orderNumber, userId, Order::markExpired);
	}

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

		// prepare 단계에서 선택한 주문 맥락을 checkout 단계와 강하게 묶는다.
		String prepareToken = orderPrepareTokenService.issueToken(
			userId,
			request.eventId(),
			request.eventCourseId(),
			request.eventPaceId()
		);
		CheckoutPrepareResponseDto.ShippingAddressInfo shippingAddress = resolveShippingAddress(userId);
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
		// prepare 응답을 받은 사용자와 선택값 그대로 checkout에 들어왔는지 먼저 검증한다.
		OrderPrepareTokenService.ValidatedPrepareToken validatedPrepareToken =
			orderPrepareTokenService.validatePrepareToken(request.prepareToken(), userId, request);

		Event event = eventRepository.findByIdOrThrow(request.eventId(), BusinessErrorCode.EVENT_NOT_FOUND);

		EventCourse course = eventCourseRepository.findByIdAndEventIdOrThrow(
			request.eventCourseId(), request.eventId(), BusinessErrorCode.COMMON_INVALID_FORMAT);

		EventPace pace = eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			request.eventPaceId(), request.eventCourseId(), BusinessErrorCode.COMMON_INVALID_FORMAT);

		OrderCheckoutEligibility eligibility = validateCheckoutEligibility(userId, request, pace);

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

		// 주문 저장 직전에 nonce를 소비해 동일 prepare token 재사용을 막는다.
		orderPrepareTokenService.consumePrepareToken(validatedPrepareToken);

		String orderNumber = "ORD-" + System.currentTimeMillis() + "-"
			+ UUID.randomUUID().toString().substring(0, 4).toUpperCase();

		Order order = Order.createPending(
			orderNumber,
			userId,
			eligibility.entryId(),
			new Order.OrderSelection(course.getId(), pace.getId()),
			buildOrderEventSnapshot(event, course, pace),
			new Order.PaymentSnapshot(itemTotalAmount, shippingFee, discountAmount, calculatedFinalAmount),
			new Order.RecipientSnapshot(
				shippingAddress.receiverName(),
				shippingAddress.label(),
				shippingAddress.phone(),
				shippingAddress.zipcode(),
				shippingAddress.address1(),
				shippingAddress.address2(),
				shippingAddress.memo()
			)
		);

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

	private OrderCheckoutEligibility validateCheckoutEligibility(Long userId, CheckoutRequestDto request, EventPace pace) {
		OrderCheckoutEligibility eligibility = orderEntryContract.resolveCheckoutEligibility(
			userId,
			request.eventId(),
			pace.getId()
		);

		if (!eligibility.canCheckout()) {
			throw new BusinessException(resolveFailureCode(eligibility.failureCode()));
		}

		if (eligibility.entryId() == null) {
			throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
		}

		return eligibility;
	}

	private void transitionPendingOrderToTerminal(String orderNumber, Long userId, Consumer<Order> transitionAction) {
		Order order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ORDER_NOT_FOUND));

		boolean transitionedFromPending = order.getOrderStatus() == OrderStatus.PENDING;
		transitionAction.accept(order);
		runPendingRollbackFollowUp(order, transitionedFromPending);
	}

	private void runPaymentConfirmedFollowUp(Order order, boolean transitionedFromPending) {
		if (!transitionedFromPending || order.getEntryId() == null) {
			return;
		}

		orderEntryContract.handlePaymentConfirmed(order.getEntryId());
	}

	private void runPendingRollbackFollowUp(Order order, boolean transitionedFromPending) {
		if (!transitionedFromPending || order.getEntryId() == null) {
			return;
		}

		orderEntryContract.rollbackPendingPayment(order.getEntryId());
	}

	private BusinessErrorCode resolveFailureCode(String failureCode) {
		if (failureCode == null || failureCode.isBlank()) {
			return BusinessErrorCode.ENTRY_CANNOT_APPLY;
		}

		for (BusinessErrorCode errorCode : BusinessErrorCode.values()) {
			if (errorCode.getCode().equals(failureCode)) {
				return errorCode;
			}
		}

		return BusinessErrorCode.COMMON_SYSTEM_ERROR;
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

	private CheckoutPrepareResponseDto.ShippingAddressInfo resolveShippingAddress(Long userId) {
		return findDefaultAddressForCheckout(userId)
			.map(this::toShippingAddressInfo)
			.orElseGet(CheckoutPrepareResponseDto.ShippingAddressInfo::empty);
	}

	private ShippingAddressSnapshot resolveShippingAddressSnapshot(Long userId, CheckoutRequestDto request) {
		if (request.addressId() != null) {
			Address address = findOwnedAddressOrThrow(userId, request.addressId());

			return toShippingAddressSnapshot(address, request.deliveryMemo());
		}

		Address defaultAddress = findDefaultAddressForCheckout(userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));

		return toShippingAddressSnapshot(defaultAddress, request.deliveryMemo());
	}

	private java.util.Optional<Address> findDefaultAddressForCheckout(Long userId) {
		return addressRepository.findFirstByUserIdAndIsDefaultTrue(userId)
			.or(() -> addressRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().findFirst());
	}

	private Address findOwnedAddressOrThrow(Long userId, Long addressId) {
		return addressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));
	}

	private CheckoutPrepareResponseDto.ShippingAddressInfo toShippingAddressInfo(Address address) {
		return new CheckoutPrepareResponseDto.ShippingAddressInfo(
			true,
			address.getId(),
			address.getReceiverName(),
			address.getPhone(),
			address.getZipcode(),
			address.getAddress1(),
			address.getAddress2(),
			address.getMemo(),
			address.isDefault()
		);
	}

	private ShippingAddressSnapshot toShippingAddressSnapshot(Address address, String overrideMemo) {
		return new ShippingAddressSnapshot(
			address.getLabel(),
			address.getReceiverName(),
			address.getPhone(),
			address.getZipcode(),
			address.getAddress1(),
			address.getAddress2(),
			overrideMemo != null ? overrideMemo : address.getMemo()
		);
	}

	private Order.OrderEventSnapshot buildOrderEventSnapshot(Event event, EventCourse course, EventPace pace) {
		return new Order.OrderEventSnapshot(
			event.getId(),
			event.getTitle(),
			event.getAppType(),
			event.getStatus(),
			event.getEventAt(),
			event.getVenue(),
			course.getName(),
			pace != null ? pace.getName() : null
		);
	}

	private String extractThumbnailUrl(Event event) {
		return event.getImages().stream()
			.filter(image -> image.getType() == EventImageType.THUMBNAIL)
			.sorted(Comparator.comparingInt(EventImage::getSort))
			.map(EventImage::getUrl)
			.findFirst()
			.orElse(null);
	}

	private OrderStatus resolveOrderStatus(String tab) {
		if ("pending".equalsIgnoreCase(tab)) {
			return OrderStatus.PENDING;
		}
		if ("completed".equalsIgnoreCase(tab)) {
			return OrderStatus.PAID;
		}

		throw new BusinessException(BusinessErrorCode.ORDER_INVALID_TAB);
	}

	private List<OrderSummaryDto> buildOrderSummaries(List<Order> orders) {
		if (orders.isEmpty()) {
			return List.of();
		}

		Map<Long, EventCourse> courseById = loadCourses(orders);
		Map<Long, EventPace> paceById = loadPaces(orders);
		Map<Long, Event> eventById = loadEvents(courseById.values());
		Map<Long, String> thumbnailByEventId = buildThumbnailMap(eventById);

		return orders.stream()
			.map(order -> {
				EventCourse course = courseById.get(order.getEventCourseId());
				Long eventId = course != null && course.getEvent() != null ? course.getEvent().getId() : null;
				Event event = eventId != null ? eventById.get(eventId) : null;
				EventPace pace = order.getEventPaceId() != null ? paceById.get(order.getEventPaceId()) : null;

				return new OrderSummaryDto(
					eventId,
					order.getOrderNumber(),
					order.getOrderStatus(),
					order.getCreatedAt(),
					order.getFinalAmount(),
					event != null ? event.getTitle() : null,
					eventId != null ? thumbnailByEventId.get(eventId) : null,
					course != null ? course.getName() : null,
					pace != null ? pace.getName() : null
				);
			})
			.toList();
	}

	private Map<Long, EventCourse> loadCourses(List<Order> orders) {
		Set<Long> courseIds = orders.stream()
			.map(Order::getEventCourseId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		if (courseIds.isEmpty()) {
			return Map.of();
		}

		return eventCourseRepository.findAllById(courseIds).stream()
			.collect(Collectors.toMap(EventCourse::getId, Function.identity()));
	}

	private Map<Long, EventPace> loadPaces(List<Order> orders) {
		Set<Long> paceIds = orders.stream()
			.map(Order::getEventPaceId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		if (paceIds.isEmpty()) {
			return Map.of();
		}

		return eventPaceRepository.findAllById(paceIds).stream()
			.collect(Collectors.toMap(EventPace::getId, Function.identity()));
	}

	private Map<Long, Event> loadEvents(Iterable<EventCourse> courses) {
		Set<Long> eventIds = java.util.stream.StreamSupport.stream(courses.spliterator(), false)
			.map(EventCourse::getEvent)
			.filter(Objects::nonNull)
			.map(Event::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		if (eventIds.isEmpty()) {
			return Map.of();
		}

		return eventRepository.findAllById(eventIds).stream()
			.collect(Collectors.toMap(Event::getId, Function.identity()));
	}

	private Map<Long, String> buildThumbnailMap(Map<Long, Event> eventById) {
		return eventById.values().stream()
			.collect(Collectors.toMap(Event::getId, this::extractThumbnailUrl));
	}

	private record ShippingAddressSnapshot(
		String label,
		String receiverName,
		String phone,
		String zipcode,
		String address1,
		String address2,
		String memo
	) {
	}

}
