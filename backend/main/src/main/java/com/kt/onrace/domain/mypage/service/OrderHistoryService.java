package com.kt.onrace.domain.mypage.service;

import static com.kt.onrace.domain.order.entity.QOrder.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.event.repository.EventCourseRepository;
import com.kt.onrace.domain.event.repository.EventPaceRepository;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.mypage.dto.MyPageOrderDetailResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderTab;
import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.kt.onrace.domain.order.repository.OrderPackageRepository;
import com.kt.onrace.domain.order.repository.OrderRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderHistoryService {

	private final OrderRepository orderRepository;
	private final OrderPackageRepository orderPackageRepository;
	private final EventCourseRepository eventCourseRepository;
	private final EventPaceRepository eventPaceRepository;
	private final EventRepository eventRepository;
	private final JPAQueryFactory queryFactory;
	private final MyPageDisplayStatusResolver displayStatusResolver;

	public MyPageOrderListResponseDto getOrders(Long userId, MyPageOrderTab tab, int page, int size) {
		MyPagePagingPolicy.validate(page, size);

		long totalCount = countOrders(userId, tab);
		if (totalCount == 0) {
			return MyPageOrderListResponseDto.empty(page, size);
		}

		List<Order> orders = queryFactory.selectFrom(order)
			.where(order.userId.eq(userId), statusCondition(tab))
			.orderBy(order.createdAt.desc())
			.offset(MyPagePagingPolicy.offset(page, size))
			.limit(size)
			.fetch();

		OrderLookup lookup = buildOrderLookup(orders);
		List<MyPageOrderItemDto> items = orders.stream()
			.map(currentOrder -> toOrderItem(currentOrder, lookup))
			.toList();

		return MyPageOrderListResponseDto.of(page, size, totalCount, items);
	}

	public MyPageOrderDetailResponseDto getOrderDetail(Long userId, String orderNumber) {
		Order currentOrder = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ORDER_NOT_FOUND));

		OrderLookup lookup = buildOrderLookup(List.of(currentOrder));
		EventCourse course = lookup.courseById().get(currentOrder.getEventCourseId());
		EventPace pace = currentOrder.getEventPaceId() != null ? lookup.paceById().get(currentOrder.getEventPaceId()) : null;
		Event currentEvent = course != null && course.getEvent() != null ? lookup.eventById().get(course.getEvent().getId()) : null;
		MyPageStatusDto status = displayStatusResolver.resolveOrderStatus(currentOrder);

		List<MyPageOrderDetailResponseDto.PackageInfo> packages = orderPackageRepository.findByOrderIdOrderByIdAsc(currentOrder.getId())
			.stream()
			.map(orderPackage -> new MyPageOrderDetailResponseDto.PackageInfo(
				orderPackage.getEventPackageId(),
				orderPackage.getName(),
				orderPackage.getPrice()
			))
			.toList();

		return new MyPageOrderDetailResponseDto(
			currentEvent != null ? currentEvent.getId() : null,
			currentOrder.getOrderNumber(),
			status.statusText(),
			status.actionType(),
			status.actionLabel(),
			status.actionEnabled(),
			currentOrder.getCreatedAt(),
			resolvePaymentDeadlineAt(currentOrder),
			currentEvent != null ? currentEvent.getTitle() : null,
			null,
			course != null ? course.getName() : null,
			pace != null ? pace.getName() : null,
			currentOrder.getItemTotalAmount(),
			currentOrder.getShippingFee(),
			currentOrder.getDiscountAmount(),
			currentOrder.getFinalAmount(),
			currentOrder.getRecipientName(),
			currentOrder.getAddressLabel(),
			currentOrder.getRecipientPhone(),
			currentOrder.getZipCode(),
			currentOrder.getAddress(),
			currentOrder.getDetailAddress(),
			currentOrder.getDeliveryMemo(),
			null,
			false,
			null,
			null,
			displayStatusResolver.canCancel(currentOrder.getOrderStatus()),
			displayStatusResolver.canRefund(currentOrder.getOrderStatus()),
			displayStatusResolver.canExchange(currentOrder.getOrderStatus()),
			packages
		);
	}

	private long countOrders(Long userId, MyPageOrderTab tab) {
		Long count = queryFactory.select(order.count())
			.from(order)
			.where(order.userId.eq(userId), statusCondition(tab))
			.fetchOne();

		return count == null ? 0 : count;
	}

	private BooleanExpression statusCondition(MyPageOrderTab tab) {
		if (tab == null || tab == MyPageOrderTab.ALL) {
			return null;
		}

		return switch (tab) {
			case ALL -> null;
			case PENDING -> order.orderStatus.eq(OrderStatus.PENDING);
			case COMPLETED -> order.orderStatus.eq(OrderStatus.PAID);
			case CANCELLED -> order.orderStatus.eq(OrderStatus.CANCELLED);
		};
	}

	private OrderLookup buildOrderLookup(List<Order> orders) {
		Set<Long> courseIds = orders.stream()
			.map(Order::getEventCourseId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Map<Long, EventCourse> courseById = courseIds.isEmpty()
			? Map.of()
			: eventCourseRepository.findAllById(courseIds).stream()
				.collect(Collectors.toMap(EventCourse::getId, Function.identity()));

		Set<Long> paceIds = orders.stream()
			.map(Order::getEventPaceId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Map<Long, EventPace> paceById = paceIds.isEmpty()
			? Map.of()
			: eventPaceRepository.findAllById(paceIds).stream()
				.collect(Collectors.toMap(EventPace::getId, Function.identity()));

		Set<Long> eventIds = courseById.values().stream()
			.map(EventCourse::getEvent)
			.filter(Objects::nonNull)
			.map(Event::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Map<Long, Event> eventById = eventIds.isEmpty()
			? Map.of()
			: eventRepository.findAllById(eventIds).stream()
				.collect(Collectors.toMap(Event::getId, Function.identity()));

		return new OrderLookup(courseById, paceById, eventById);
	}

	private MyPageOrderItemDto toOrderItem(Order currentOrder, OrderLookup lookup) {
		EventCourse course = lookup.courseById().get(currentOrder.getEventCourseId());
		EventPace pace = currentOrder.getEventPaceId() != null ? lookup.paceById().get(currentOrder.getEventPaceId()) : null;
		Event currentEvent = course != null && course.getEvent() != null ? lookup.eventById().get(course.getEvent().getId()) : null;
		MyPageStatusDto status = displayStatusResolver.resolveOrderStatus(currentOrder);

		return new MyPageOrderItemDto(
			currentOrder.getOrderNumber(),
			currentEvent != null ? currentEvent.getId() : null,
			status.statusText(),
			status.actionType(),
			status.actionLabel(),
			status.actionEnabled(),
			null,
			currentEvent != null ? currentEvent.getTitle() : null,
			course != null ? course.getName() : null,
			pace != null ? pace.getName() : null,
			currentOrder.getFinalAmount(),
			currentOrder.getCreatedAt(),
			resolvePaymentDeadlineAt(currentOrder)
		);
	}

	private LocalDateTime resolvePaymentDeadlineAt(Order currentOrder) {
		// MVP에서는 실제 결제 마감 시각 원천이 아직 없어 null을 유지한다.
		if (currentOrder.getOrderStatus() != OrderStatus.PENDING) {
			return null;
		}

		return null;
	}

	private record OrderLookup(
		Map<Long, EventCourse> courseById,
		Map<Long, EventPace> paceById,
		Map<Long, Event> eventById
	) {
	}
}
