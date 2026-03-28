package com.kt.onrace.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventImage;
import com.kt.onrace.domain.event.entity.EventImageType;
import com.kt.onrace.domain.event.entity.EventItem;
import com.kt.onrace.domain.event.entity.EventItemType;
import com.kt.onrace.domain.event.entity.EventPackage;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.event.entity.EventRegion;
import com.kt.onrace.domain.event.entity.EventType;
import com.kt.onrace.domain.event.repository.EventCourseRepository;
import com.kt.onrace.domain.event.repository.EventPackageRepository;
import com.kt.onrace.domain.event.repository.EventPaceRepository;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.order.contract.OrderCheckoutEligibility;
import com.kt.onrace.domain.order.contract.OrderEntryContract;
import com.kt.onrace.domain.order.dto.CheckoutPrepareRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutPrepareResponseDto;
import com.kt.onrace.domain.order.dto.CheckoutRequestDto;
import com.kt.onrace.domain.order.dto.OrderDetailResponseDto;
import com.kt.onrace.domain.order.dto.OrderListResponseDto;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderPackage;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.kt.onrace.domain.order.repository.OrderPackageRepository;
import com.kt.onrace.domain.order.repository.OrderRepository;
import com.kt.onrace.domain.order.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private EventRepository eventRepository;

	@Mock
	private EventCourseRepository eventCourseRepository;

	@Mock
	private EventPaceRepository eventPaceRepository;

	@Mock
	private EventPackageRepository eventPackageRepository;

	@Mock
	private AddressRepository addressRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderPackageRepository orderPackageRepository;

	@Mock
	private OrderEntryContract orderEntryContract;

	@InjectMocks
	private OrderService orderService;

	@Captor
	private ArgumentCaptor<Order> orderCaptor;

	@Test
	@DisplayName("checkout-info는 addressId가 없으면 기본 배송지를 내려준다")
	void checkoutPrepareReturnsDefaultAddress() {
		TestFixture fixture = createFixture();

		when(eventRepository.findByIdOrThrow(eq(1L), eq(BusinessErrorCode.EVENT_NOT_FOUND))).thenReturn(fixture.event());
		when(eventCourseRepository.findByIdAndEventIdOrThrow(
			eq(10L), eq(1L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.course());
		when(eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			eq(20L), eq(10L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.pace());
		when(eventPackageRepository.findByEventId(eq(1L))).thenReturn(List.of(fixture.eventPackage()));
		when(addressRepository.findFirstByUserIdAndIsDefaultTrue(eq(7L))).thenReturn(Optional.of(fixture.defaultAddress()));

		CheckoutPrepareResponseDto response = orderService.getCheckoutPrepareInfo(
			new CheckoutPrepareRequestDto(1L, 10L, 20L),
			7L
		);

		assertThat(response.orderRequestInfo().eventName()).isEqualTo("서울 마라톤");
		assertThat(response.orderRequestInfo().courseName()).isEqualTo("하프코스");
		assertThat(response.packages()).hasSize(1);
		assertThat(response.shippingAddress().hasAddress()).isTrue();
		assertThat(response.shippingAddress().addressId()).isEqualTo(100L);
		assertThat(response.shippingAddress().receiverName()).isEqualTo("기본배송지");
		assertThat(response.paymentDetail().finalAmount()).isEqualTo(53000L);
	}

	@Test
	@DisplayName("checkout-info는 기본 배송지가 없으면 최근 배송지를 내려준다")
	void checkoutPrepareReturnsLatestAddressWhenDefaultMissing() {
		TestFixture fixture = createFixture();

		when(eventRepository.findByIdOrThrow(eq(1L), eq(BusinessErrorCode.EVENT_NOT_FOUND))).thenReturn(fixture.event());
		when(eventCourseRepository.findByIdAndEventIdOrThrow(
			eq(10L), eq(1L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.course());
		when(eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			eq(20L), eq(10L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.pace());
		when(eventPackageRepository.findByEventId(eq(1L))).thenReturn(List.of(fixture.eventPackage()));
		when(addressRepository.findFirstByUserIdAndIsDefaultTrue(eq(7L))).thenReturn(Optional.empty());
		when(addressRepository.findByUserIdOrderByCreatedAtDesc(eq(7L))).thenReturn(List.of(fixture.selectedAddress()));

		CheckoutPrepareResponseDto response = orderService.getCheckoutPrepareInfo(
			new CheckoutPrepareRequestDto(1L, 10L, 20L),
			7L
		);

		assertThat(response.shippingAddress().hasAddress()).isTrue();
		assertThat(response.shippingAddress().addressId()).isEqualTo(101L);
		assertThat(response.shippingAddress().receiverName()).isEqualTo("선택배송지");
	}

	@Test
	@DisplayName("checkout-info는 저장된 배송지가 없으면 빈 배송지 정보를 내려준다")
	void checkoutPrepareReturnsEmptyWhenAddressDoesNotExist() {
		TestFixture fixture = createFixture();

		when(eventRepository.findByIdOrThrow(eq(1L), eq(BusinessErrorCode.EVENT_NOT_FOUND))).thenReturn(fixture.event());
		when(eventCourseRepository.findByIdAndEventIdOrThrow(
			eq(10L), eq(1L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.course());
		when(eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			eq(20L), eq(10L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.pace());
		when(eventPackageRepository.findByEventId(eq(1L))).thenReturn(List.of(fixture.eventPackage()));
		when(addressRepository.findFirstByUserIdAndIsDefaultTrue(eq(7L))).thenReturn(Optional.empty());
		when(addressRepository.findByUserIdOrderByCreatedAtDesc(eq(7L))).thenReturn(List.of());

		CheckoutPrepareResponseDto response = orderService.getCheckoutPrepareInfo(
			new CheckoutPrepareRequestDto(1L, 10L, 20L),
			7L
		);

		assertThat(response.shippingAddress().hasAddress()).isFalse();
		assertThat(response.shippingAddress().addressId()).isNull();
	}

	@Test
	@DisplayName("checkout-info는 사용자별 주소를 조회하고 없으면 빈 배송지 정보를 내려준다")
	void checkoutPrepareReturnsEmptyWhenUserHasNoAddress() {
		TestFixture fixture = createFixture();

		when(eventRepository.findByIdOrThrow(eq(1L), eq(BusinessErrorCode.EVENT_NOT_FOUND))).thenReturn(fixture.event());
		when(eventCourseRepository.findByIdAndEventIdOrThrow(
			eq(10L), eq(1L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.course());
		when(eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			eq(20L), eq(10L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.pace());
		when(eventPackageRepository.findByEventId(eq(1L))).thenReturn(List.of(fixture.eventPackage()));
		when(addressRepository.findFirstByUserIdAndIsDefaultTrue(eq(8L))).thenReturn(Optional.empty());
		when(addressRepository.findByUserIdOrderByCreatedAtDesc(eq(8L))).thenReturn(List.of());

		CheckoutPrepareResponseDto response = orderService.getCheckoutPrepareInfo(
			new CheckoutPrepareRequestDto(1L, 10L, 20L),
			8L
		);

		assertThat(response.shippingAddress().hasAddress()).isFalse();
		assertThat(response.shippingAddress().addressId()).isNull();
	}

	@Test
	@DisplayName("checkout은 선택한 배송지 정보를 주문 payload에 반영한다")
	void checkoutUsesSelectedAddressSnapshot() {
		TestFixture fixture = createFixture();

		when(eventRepository.findByIdOrThrow(eq(1L), eq(BusinessErrorCode.EVENT_NOT_FOUND))).thenReturn(fixture.event());
		when(eventCourseRepository.findByIdAndEventIdOrThrow(
			eq(10L), eq(1L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.course());
		when(eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			eq(20L), eq(10L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.pace());
		mockCheckoutEligibility(7L, true, null);
		when(eventPackageRepository.findAllById(eq(List.of(30L)))).thenReturn(List.of(fixture.eventPackage()));
		when(addressRepository.findByIdAndUserId(eq(101L), eq(7L))).thenReturn(Optional.of(fixture.selectedAddress()));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		orderService.checkout(
			new CheckoutRequestDto(
				"prepare-token",
				1L,
				10L,
				20L,
				List.of(30L),
				63000L,
				101L,
				null,
				null,
				null,
				null,
				null,
				"직접 입력 메모"
			),
			7L
		);

		org.mockito.Mockito.verify(orderRepository).save(orderCaptor.capture());
		Order savedOrder = orderCaptor.getValue();

		assertThat(savedOrder.getRecipientName()).isEqualTo("선택배송지");
		assertThat(savedOrder.getAddressLabel()).isEqualTo("회사");
		assertThat(savedOrder.getRecipientPhone()).isEqualTo("010-2222-3333");
		assertThat(savedOrder.getZipCode()).isEqualTo("54321");
		assertThat(savedOrder.getAddress()).isEqualTo("서울시 송파구");
		assertThat(savedOrder.getDetailAddress()).isEqualTo("202동");
		assertThat(savedOrder.getDeliveryMemo()).isEqualTo("직접 입력 메모");
		assertThat(savedOrder.getFinalAmount()).isEqualTo(63000L);
	}

	@Test
	@DisplayName("checkout은 addressId가 없으면 기본 배송지 스냅샷을 사용한다")
	void checkoutUsesDefaultAddressSnapshotWhenAddressIdMissing() {
		TestFixture fixture = createFixture();

		when(eventRepository.findByIdOrThrow(eq(1L), eq(BusinessErrorCode.EVENT_NOT_FOUND))).thenReturn(fixture.event());
		when(eventCourseRepository.findByIdAndEventIdOrThrow(
			eq(10L), eq(1L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.course());
		when(eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			eq(20L), eq(10L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.pace());
		mockCheckoutEligibility(7L, true, null);
		when(eventPackageRepository.findAllById(eq(List.of(30L)))).thenReturn(List.of(fixture.eventPackage()));
		when(addressRepository.findFirstByUserIdAndIsDefaultTrue(eq(7L))).thenReturn(Optional.of(fixture.defaultAddress()));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		orderService.checkout(
			new CheckoutRequestDto(
				"prepare-token",
				1L,
				10L,
				20L,
				List.of(30L),
				63000L,
				null,
				null,
				null,
				null,
				null,
				null,
				"직접 입력 메모"
			),
			7L
		);

		org.mockito.Mockito.verify(orderRepository).save(orderCaptor.capture());
		Order savedOrder = orderCaptor.getValue();

		assertThat(savedOrder.getRecipientName()).isEqualTo("기본배송지");
		assertThat(savedOrder.getAddressLabel()).isEqualTo("우리 집");
		assertThat(savedOrder.getRecipientPhone()).isEqualTo("010-0000-0000");
		assertThat(savedOrder.getZipCode()).isEqualTo("12345");
		assertThat(savedOrder.getAddress()).isEqualTo("서울시 강남구");
		assertThat(savedOrder.getDetailAddress()).isEqualTo("101동");
		assertThat(savedOrder.getDeliveryMemo()).isEqualTo("직접 입력 메모");
	}

	@Test
	@DisplayName("checkout은 저장된 배송지가 없으면 ADDRESS_NOT_FOUND 예외가 발생한다")
	void checkoutThrowsWhenNoSavedAddressExists() {
		TestFixture fixture = createFixture();

		when(eventRepository.findByIdOrThrow(eq(1L), eq(BusinessErrorCode.EVENT_NOT_FOUND))).thenReturn(fixture.event());
		when(eventCourseRepository.findByIdAndEventIdOrThrow(
			eq(10L), eq(1L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.course());
		when(eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			eq(20L), eq(10L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.pace());
		mockCheckoutEligibility(7L, true, null);
		when(eventPackageRepository.findAllById(eq(List.of(30L)))).thenReturn(List.of(fixture.eventPackage()));
		when(addressRepository.findFirstByUserIdAndIsDefaultTrue(eq(7L))).thenReturn(Optional.empty());
		when(addressRepository.findByUserIdOrderByCreatedAtDesc(eq(7L))).thenReturn(List.of());

		assertThatThrownBy(() -> orderService.checkout(
			new CheckoutRequestDto(
				"prepare-token",
				1L,
				10L,
				20L,
				List.of(30L),
				63000L,
				null,
				"직접입력",
				"01012345678",
				"12345",
				"서울 어딘가",
				"101동",
				"직접 입력 메모"
			),
			7L
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_NOT_FOUND);
	}

	@Test
	@DisplayName("checkout은 다른 유저 배송지 id면 ADDRESS_NOT_FOUND 예외가 발생한다")
	void checkoutThrowsWhenAddressBelongsToAnotherUser() {
		TestFixture fixture = createFixture();

		when(eventRepository.findByIdOrThrow(eq(1L), eq(BusinessErrorCode.EVENT_NOT_FOUND))).thenReturn(fixture.event());
		when(eventCourseRepository.findByIdAndEventIdOrThrow(
			eq(10L), eq(1L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.course());
		when(eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			eq(20L), eq(10L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.pace());
		mockCheckoutEligibility(8L, true, null);
		when(eventPackageRepository.findAllById(eq(List.of(30L)))).thenReturn(List.of(fixture.eventPackage()));
		when(addressRepository.findByIdAndUserId(eq(101L), eq(8L))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.checkout(
			new CheckoutRequestDto(
				"prepare-token",
				1L,
				10L,
				20L,
				List.of(30L),
				63000L,
				101L,
				null,
				null,
				null,
				null,
				null,
				"직접 입력 메모"
			),
			8L
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.ADDRESS_NOT_FOUND);
	}

	@Test
	@DisplayName("checkout은 entry eligibility가 false면 failureCode 기반 예외를 반환한다")
	void checkoutThrowsWhenEntryEligibilityFails() {
		TestFixture fixture = createFixture();

		when(eventRepository.findByIdOrThrow(eq(1L), eq(BusinessErrorCode.EVENT_NOT_FOUND))).thenReturn(fixture.event());
		when(eventCourseRepository.findByIdAndEventIdOrThrow(
			eq(10L), eq(1L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.course());
		when(eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			eq(20L), eq(10L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.pace());
		mockCheckoutEligibility(7L, false, BusinessErrorCode.ENTRY_CANNOT_APPLY.getCode());

		assertThatThrownBy(() -> orderService.checkout(
			new CheckoutRequestDto(
				"prepare-token",
				1L,
				10L,
				20L,
				List.of(),
				53000L,
				null,
				null,
				null,
				null,
				null,
				null,
				"직접 입력 메모"
			),
			7L
		))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.ENTRY_CANNOT_APPLY);
	}

	@Test
	@DisplayName("목록 조회는 pending 탭에 대해 주문 요약 정보를 반환한다")
	void getOrdersReturnsPendingSummaries() {
		TestFixture fixture = createFixture();
		Order pendingOrder = createOrder("ORD-PENDING-001", 7L, 10L, 20L, OrderStatus.PENDING, 53000L);
		setCreatedAt(pendingOrder, LocalDateTime.of(2026, 2, 10, 12, 30));

		when(orderRepository.findByUserIdAndOrderStatusOrderByCreatedAtDesc(eq(7L), eq(OrderStatus.PENDING)))
			.thenReturn(List.of(pendingOrder));
		when(eventCourseRepository.findAllById(eq(Set.of(10L)))).thenReturn(List.of(fixture.course()));
		when(eventPaceRepository.findAllById(eq(Set.of(20L)))).thenReturn(List.of(fixture.pace()));
		when(eventRepository.findAllById(eq(Set.of(1L)))).thenReturn(List.of(fixture.event()));

		OrderListResponseDto response = orderService.getOrders("pending", 7L);

		assertThat(response.orders()).hasSize(1);
		assertThat(response.orders().getFirst().eventId()).isEqualTo(1L);
		assertThat(response.orders().getFirst().orderNumber()).isEqualTo("ORD-PENDING-001");
		assertThat(response.orders().getFirst().eventTitle()).isEqualTo("서울 마라톤");
		assertThat(response.orders().getFirst().courseName()).isEqualTo("하프코스");
		assertThat(response.orders().getFirst().paceName()).isEqualTo("05:30");
		assertThat(response.orders().getFirst().thumbnailUrl()).isEqualTo("https://example.com/thumb.png");
	}

	@Test
	@DisplayName("목록 조회는 completed 탭을 PAID 상태로 매핑한다")
	void getOrdersMapsCompletedTabToPaid() {
		TestFixture fixture = createFixture();
		Order paidOrder = createOrder("ORD-PAID-001", 7L, 10L, 20L, OrderStatus.PAID, 53000L);
		setCreatedAt(paidOrder, LocalDateTime.of(2026, 2, 11, 9, 0));

		when(orderRepository.findByUserIdAndOrderStatusOrderByCreatedAtDesc(eq(7L), eq(OrderStatus.PAID)))
			.thenReturn(List.of(paidOrder));
		when(eventCourseRepository.findAllById(eq(Set.of(10L)))).thenReturn(List.of(fixture.course()));
		when(eventPaceRepository.findAllById(eq(Set.of(20L)))).thenReturn(List.of(fixture.pace()));
		when(eventRepository.findAllById(eq(Set.of(1L)))).thenReturn(List.of(fixture.event()));

		OrderListResponseDto response = orderService.getOrders("completed", 7L);

		assertThat(response.orders()).hasSize(1);
		assertThat(response.orders().getFirst().orderStatus()).isEqualTo(OrderStatus.PAID);
	}

	@Test
	@DisplayName("상세 조회는 주문 상품, 배송지, 패키지 스냅샷을 반환한다")
	void getOrderDetailReturnsSnapshot() {
		TestFixture fixture = createFixture();
		Order order = createOrder("ORD-DETAIL-001", 7L, 10L, 20L, OrderStatus.PAID, 63000L);
		setId(order, 200L);
		setCreatedAt(order, LocalDateTime.of(2026, 2, 15, 14, 30));

		OrderPackage orderPackage = OrderPackage.builder()
			.eventPackageId(30L)
			.name("기념 티셔츠")
			.price(10000L)
			.build();

		when(orderRepository.findByOrderNumberAndUserId(eq("ORD-DETAIL-001"), eq(7L))).thenReturn(Optional.of(order));
		when(eventCourseRepository.findAllById(eq(Set.of(10L)))).thenReturn(List.of(fixture.course()));
		when(eventPaceRepository.findAllById(eq(Set.of(20L)))).thenReturn(List.of(fixture.pace()));
		when(eventRepository.findAllById(eq(Set.of(1L)))).thenReturn(List.of(fixture.event()));
		when(orderPackageRepository.findByOrderIdOrderByIdAsc(eq(200L))).thenReturn(List.of(orderPackage));

		OrderDetailResponseDto response = orderService.getOrderDetail("ORD-DETAIL-001", 7L);

		assertThat(response.orderNumber()).isEqualTo("ORD-DETAIL-001");
		assertThat(response.eventTitle()).isEqualTo("서울 마라톤");
		assertThat(response.courseName()).isEqualTo("하프코스");
		assertThat(response.paceName()).isEqualTo("05:30");
		assertThat(response.recipientName()).isEqualTo("홍길동");
		assertThat(response.addressLabel()).isEqualTo("우리 집");
		assertThat(response.recipientPhone()).isEqualTo("010-1111-2222");
		assertThat(response.address()).isEqualTo("서울시 마포구");
		assertThat(response.packages()).hasSize(1);
		assertThat(response.packages().getFirst().name()).isEqualTo("기념 티셔츠");
	}

	@Test
	@DisplayName("상세 조회는 없는 주문번호면 ORDER_NOT_FOUND 예외가 발생한다")
	void getOrderDetailThrowsWhenOrderDoesNotExist() {
		when(orderRepository.findByOrderNumberAndUserId(eq("ORD-NOT-FOUND"), eq(7L))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.getOrderDetail("ORD-NOT-FOUND", 7L))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.ORDER_NOT_FOUND);
	}

	@Test
	@DisplayName("목록 조회는 허용되지 않은 탭이면 ORDER_INVALID_TAB 예외가 발생한다")
	void getOrdersThrowsWhenTabIsInvalid() {
		assertThatThrownBy(() -> orderService.getOrders("archived", 7L))
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.ORDER_INVALID_TAB);
	}

	private TestFixture createFixture() {
		Event event = Event.builder()
			.title("서울 마라톤")
			.type(EventType.MARATHON)
			.appType(EventAppType.LOTTERY)
			.eventAt(LocalDateTime.of(2026, 3, 1, 9, 0))
			.appStartAt(LocalDateTime.of(2026, 2, 1, 10, 0))
			.appEndAt(LocalDateTime.of(2026, 2, 10, 18, 0))
			.region(EventRegion.SEOUL)
			.venue("여의도공원")
			.isView(true)
			.soldOut(false)
			.build();
		setId(event, 1L);

		EventImage thumbnailImage = EventImage.builder()
			.event(event)
			.type(EventImageType.THUMBNAIL)
			.url("https://example.com/thumb.png")
			.sort(1)
			.build();
		setId(thumbnailImage, 40L);
		event.getImages().add(thumbnailImage);

		EventCourse course = EventCourse.builder()
			.event(event)
			.name("하프코스")
			.mapUrl("https://example.com/map")
			.distanceMeter(21097)
			.timeLimit(180)
			.waterSource(5)
			.altitude(100)
			.courseRoute("잠실-여의도 하프 코스")
			.price(50000L)
			.build();
		setId(course, 10L);
		event.getCourses().add(course);

		EventPace pace = EventPace.builder()
			.eventCourse(course)
			.name("05:30")
			.hour(5)
			.minutes(30)
			.capacity(100)
			.build();
		setId(pace, 20L);
		course.getEventPaces().add(pace);

		EventItem eventItem = EventItem.builder()
			.name("기념 티셔츠")
			.price(10000L)
			.description("기본 옵션")
			.build();
		setId(eventItem, 31L);

		EventPackage eventPackage = EventPackage.builder()
			.event(event)
			.item(eventItem)
			.itemType(EventItemType.ADDITIONAL)
			.build();
		setId(eventPackage, 30L);
		event.getPackages().add(eventPackage);

		Address defaultAddress = Address.builder()
			.userId(7L)
			.label("우리 집")
			.normalizedLabel("우리 집")
			.receiverName("기본배송지")
			.phone("010-0000-0000")
			.zipcode("12345")
			.address1("서울시 강남구")
			.address2("101동")
			.memo("문앞")
			.isDefault(true)
			.build();
		setId(defaultAddress, 100L);

		Address selectedAddress = Address.builder()
			.userId(7L)
			.label("회사")
			.normalizedLabel("회사")
			.receiverName("선택배송지")
			.phone("010-2222-3333")
			.zipcode("54321")
			.address1("서울시 송파구")
			.address2("202동")
			.memo("경비실")
			.isDefault(false)
			.build();
		setId(selectedAddress, 101L);

		return new TestFixture(event, course, pace, eventPackage, defaultAddress, selectedAddress);
	}

	private void mockCheckoutEligibility(Long userId, boolean canCheckout, String failureCode) {
		when(orderEntryContract.resolveCheckoutEligibility(eq(userId), eq(1L), eq(20L)))
			.thenReturn(OrderCheckoutEligibility.builder()
				.entryId(1000L)
				.appType(EventAppType.LOTTERY)
				.currentEntryStatus(com.kt.onrace.domain.entry.entity.EntryStatus.WON)
				.requiredEntryStatus(com.kt.onrace.domain.entry.entity.EntryStatus.WON)
				.requiresReservationValidation(false)
				.canCheckout(canCheckout)
				.failureCode(failureCode)
				.build());
	}

	private void setId(Object target, Long id) {
		try {
			Field field = target.getClass().getSuperclass().getDeclaredField("id");
			field.setAccessible(true);
			field.set(target, id);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private void setCreatedAt(Object target, LocalDateTime createdAt) {
		try {
			Field field = target.getClass().getSuperclass().getSuperclass().getDeclaredField("createdAt");
			field.setAccessible(true);
			field.set(target, createdAt);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private Order createOrder(String orderNumber, Long userId, Long eventCourseId, Long eventPaceId,
		OrderStatus orderStatus, Long finalAmount) {
		return Order.builder()
			.orderNumber(orderNumber)
			.userId(userId)
			.eventCourseId(eventCourseId)
			.eventPaceId(eventPaceId)
			.orderStatus(orderStatus)
			.itemTotalAmount(60000L)
			.shippingFee(3000L)
			.discountAmount(0L)
			.finalAmount(finalAmount)
			.recipientName("홍길동")
			.addressLabel("우리 집")
			.recipientPhone("010-1111-2222")
			.zipCode("04100")
			.address("서울시 마포구")
			.detailAddress("301호")
			.deliveryMemo("문앞에 놓아주세요")
			.build();
	}

	private record TestFixture(
		Event event,
		EventCourse course,
		EventPace pace,
		EventPackage eventPackage,
		Address defaultAddress,
		Address selectedAddress
	) {
	}
}
