package com.kt.onrace.domain.mypage.facade;

import static com.kt.onrace.domain.entry.entity.QEntry.entry;
import static com.kt.onrace.domain.event.entity.QEvent.event;
import static com.kt.onrace.domain.event.entity.QEventCourse.eventCourse;
import static com.kt.onrace.domain.event.entity.QEventPace.eventPace;
import static com.kt.onrace.domain.order.entity.QOrder.order;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;
import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventImage;
import com.kt.onrace.domain.event.entity.EventImageType;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.kt.onrace.domain.event.repository.EventCourseRepository;
import com.kt.onrace.domain.event.repository.EventPaceRepository;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.member.repository.MemberRepository;
import com.kt.onrace.domain.mypage.dto.MyPageAddressResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOverviewResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * MyPage는 별도 쓰기 도메인이 아니라 address/entry/order/event를 조합하는 read model 계층이다.
 * 현재는 기존 repository 재사용을 우선하고, 복잡한 projection 최적화는 추후 티켓에서 다룬다.
 */
public class MyPageFacade {

	private static final String ACTION_NONE = "NONE";
	private static final String ACTION_EDIT = "EDIT";
	private static final String ACTION_CHECKOUT = "CHECKOUT";
	private static final String ACTION_DETAIL = "DETAIL";

	private final MemberRepository memberRepository;
	private final AddressRepository addressRepository;
	private final EventRepository eventRepository;
	private final EventCourseRepository eventCourseRepository;
	private final EventPaceRepository eventPaceRepository;
	private final JPAQueryFactory queryFactory;

	public MyPageOverviewResponseDto getOverview(Long userId) {
		validateMember(userId);
		List<Entry> entries = findUserEntries(userId);
		List<Order> orders = findUserOrders(userId);

		return new MyPageOverviewResponseDto(
			buildEntriesResponse(entries),
			buildWaitingEntriesResponse(entries),
			buildOrdersResponse(orders),
			buildAddressResponse(userId)
		);
	}

	public MyPageEntryListResponseDto getEntries(Long userId) {
		validateMember(userId);
		return buildEntriesResponse(findUserEntries(userId));
	}

	public MyPageEntryListResponseDto getWaitingEntries(Long userId) {
		validateMember(userId);
		return buildWaitingEntriesResponse(findUserEntries(userId));
	}

	public MyPageOrderListResponseDto getOrders(Long userId) {
		validateMember(userId);
		return buildOrdersResponse(findUserOrders(userId));
	}

	public MyPageAddressResponseDto getAddress(Long userId) {
		validateMember(userId);
		return buildAddressResponse(userId);
	}

	private MyPageEntryListResponseDto buildEntriesResponse(List<Entry> entries) {
		List<MyPageEntryItemDto> items = entries.stream()
			.filter(this::isVisibleInEntriesTab)
			.map(this::toEntryItem)
			.toList();

		return MyPageEntryListResponseDto.from(items);
	}

	private MyPageEntryListResponseDto buildWaitingEntriesResponse(List<Entry> entries) {
		List<MyPageEntryItemDto> items = entries.stream()
			.filter(this::isVisibleInWaitingTab)
			.map(this::toEntryItem)
			.toList();

		return MyPageEntryListResponseDto.from(items);
	}

	private MyPageOrderListResponseDto buildOrdersResponse(List<Order> orders) {
		OrderLookup lookup = buildOrderLookup(orders);
		List<MyPageOrderItemDto> items = orders.stream()
			.map(currentOrder -> toOrderItem(currentOrder, lookup))
			.toList();

		return MyPageOrderListResponseDto.from(items);
	}

	private MyPageAddressResponseDto buildAddressResponse(Long userId) {
		List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
		if (addresses.isEmpty()) {
			return MyPageAddressResponseDto.empty();
		}

		Address defaultAddress = addresses.stream()
			.filter(Address::isDefault)
			.findFirst()
			.orElse(addresses.get(0));

		return MyPageAddressResponseDto.from(defaultAddress);
	}

	private void validateMember(Long userId) {
		memberRepository.findByIdAndIsDeletedFalseOrThrow(userId, BusinessErrorCode.MEMBER_NOT_FOUND);
	}

	private List<Entry> findUserEntries(Long userId) {
		return queryFactory.selectFrom(entry)
			.join(entry.event, event).fetchJoin()
			.join(entry.eventCourse, eventCourse).fetchJoin()
			.join(entry.eventPace, eventPace).fetchJoin()
			.where(entry.userId.eq(userId))
			.orderBy(entry.createdAt.desc())
			.fetch();
	}

	private boolean isVisibleInEntriesTab(Entry currentEntry) {
		return switch (currentEntry.getStatus()) {
			case APPLIED, WON, LOST -> true;
			case PRE_SAVED, RESERVED -> false;
		};
	}

	private boolean isVisibleInWaitingTab(Entry currentEntry) {
		return switch (currentEntry.getStatus()) {
			case PRE_SAVED, RESERVED -> true;
			case APPLIED, WON, LOST -> false;
		};
	}

	private MyPageEntryItemDto toEntryItem(Entry currentEntry) {
		Event currentEvent = currentEntry.getEvent();
		MyPageStatusDto status = resolveEntryStatus(currentEntry, currentEvent);

		return new MyPageEntryItemDto(
			currentEntry.getId(),
			currentEvent.getId(),
			status.statusText(),
			status.actionType(),
			status.actionLabel(),
			status.actionEnabled(),
			resolveThumbnailUrl(currentEvent),
			currentEvent.getTitle(),
			currentEntry.getEventCourse() != null ? currentEntry.getEventCourse().getName() : null,
			currentEntry.getEventPace() != null ? currentEntry.getEventPace().getName() : null,
			currentEntry.getEventCourse() != null ? currentEntry.getEventCourse().getPrice() : null,
			currentEntry.getCreatedAt(),
			currentEvent.getLotteryAnnouncedAt()
		);
	}

	private MyPageStatusDto resolveEntryStatus(Entry currentEntry, Event currentEvent) {
		EntryStatus entryStatus = currentEntry.getStatus();
		EventStatus eventStatus = currentEvent.getStatus();

		if (entryStatus == EntryStatus.PRE_SAVED) {
			return MyPageStatusDto.of("신청 대기", ACTION_EDIT, "사전정보 수정", true);
		}

		if (entryStatus == EntryStatus.RESERVED) {
			return MyPageStatusDto.of("예약 중", ACTION_CHECKOUT, "결제하기", true);
		}

		if (entryStatus == EntryStatus.APPLIED) {
			if (currentEvent.getAppType() == EventAppType.LOTTERY) {
				if (eventStatus == EventStatus.IN_PROGRESS || eventStatus == EventStatus.CLOSING_SOON) {
					return MyPageStatusDto.of("응모 완료", ACTION_NONE, null, false);
				}

				if (eventStatus == EventStatus.END) {
					return MyPageStatusDto.of("결과 발표 대기", ACTION_NONE, null, false);
				}

				if (eventStatus == EventStatus.DRAW_COMPLETED) {
					return MyPageStatusDto.of("결과 확인 필요", ACTION_NONE, null, false);
				}
			}

			return MyPageStatusDto.of("신청 완료", ACTION_NONE, null, false);
		}

		if (entryStatus == EntryStatus.WON) {
			return MyPageStatusDto.of("당첨", ACTION_CHECKOUT, "결제하기", true);
		}

		if (entryStatus == EntryStatus.LOST) {
			return MyPageStatusDto.of("미당첨", ACTION_NONE, null, false);
		}

		return MyPageStatusDto.of(entryStatus.getDescription(), ACTION_NONE, null, false);
	}

	private List<Order> findUserOrders(Long userId) {
		return queryFactory.selectFrom(order)
			.where(order.userId.eq(userId))
			.orderBy(order.createdAt.desc())
			.fetch();
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
		MyPageStatusDto status = resolveOrderStatus(currentOrder);

		return new MyPageOrderItemDto(
			currentOrder.getOrderNumber(),
			currentEvent != null ? currentEvent.getId() : null,
			status.statusText(),
			status.actionType(),
			status.actionLabel(),
			status.actionEnabled(),
			currentEvent != null ? resolveThumbnailUrl(currentEvent) : null,
			currentEvent != null ? currentEvent.getTitle() : null,
			course != null ? course.getName() : null,
			pace != null ? pace.getName() : null,
			currentOrder.getFinalAmount(),
			currentOrder.getCreatedAt(),
			resolvePaymentDeadlineAt(currentOrder)
		);
	}

	private MyPageStatusDto resolveOrderStatus(Order currentOrder) {
		return switch (currentOrder.getOrderStatus()) {
			case PENDING -> MyPageStatusDto.of("결제 대기", ACTION_DETAIL, "주문 상세보기", true);
			case PAID -> MyPageStatusDto.of("결제 완료", ACTION_DETAIL, "주문 상세보기", true);
			case CANCELLED -> MyPageStatusDto.of("주문 취소", ACTION_DETAIL, "주문 상세보기", true);
			case EXPIRED -> MyPageStatusDto.of("주문 만료", ACTION_DETAIL, "주문 상세보기", true);
			case FAILED -> MyPageStatusDto.of("주문 실패", ACTION_DETAIL, "주문 상세보기", true);
		};
	}

	private LocalDateTime resolvePaymentDeadlineAt(Order currentOrder) {
		if (currentOrder.getOrderStatus() != OrderStatus.PENDING) {
			return null;
		}

		return null;
	}

	private String resolveThumbnailUrl(Event currentEvent) {
		return currentEvent.getImages().stream()
			.filter(image -> image.getType() == EventImageType.THUMBNAIL)
			.sorted(Comparator.comparingInt(EventImage::getSort))
			.map(EventImage::getUrl)
			.findFirst()
			.orElse(null);
	}

	private record OrderLookup(
		Map<Long, EventCourse> courseById,
		Map<Long, EventPace> paceById,
		Map<Long, Event> eventById
	) {
	}
}
