package com.kt.onrace.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventPackage;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.event.entity.EventRegion;
import com.kt.onrace.domain.event.entity.EventType;
import com.kt.onrace.domain.event.repository.EventCourseRepository;
import com.kt.onrace.domain.event.repository.EventPackageRepository;
import com.kt.onrace.domain.event.repository.EventPaceRepository;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.order.dto.CheckoutPrepareRequestDto;
import com.kt.onrace.domain.order.dto.CheckoutPrepareResponseDto;
import com.kt.onrace.domain.order.dto.CheckoutRequestDto;
import com.kt.onrace.domain.order.entity.Order;
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
			new CheckoutPrepareRequestDto(1L, 10L, 20L, null),
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
	@DisplayName("checkout-info는 선택한 배송지 id가 있으면 해당 배송지를 내려준다")
	void checkoutPrepareReturnsSelectedAddress() {
		TestFixture fixture = createFixture();

		when(eventRepository.findByIdOrThrow(eq(1L), eq(BusinessErrorCode.EVENT_NOT_FOUND))).thenReturn(fixture.event());
		when(eventCourseRepository.findByIdAndEventIdOrThrow(
			eq(10L), eq(1L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.course());
		when(eventPaceRepository.findByIdAndEventCourseIdOrThrow(
			eq(20L), eq(10L), eq(BusinessErrorCode.COMMON_INVALID_FORMAT))).thenReturn(fixture.pace());
		when(eventPackageRepository.findByEventId(eq(1L))).thenReturn(List.of(fixture.eventPackage()));
		when(addressRepository.findByIdAndUserId(eq(101L), eq(7L))).thenReturn(Optional.of(fixture.selectedAddress()));

		CheckoutPrepareResponseDto response = orderService.getCheckoutPrepareInfo(
			new CheckoutPrepareRequestDto(1L, 10L, 20L, 101L),
			7L
		);

		assertThat(response.shippingAddress().hasAddress()).isTrue();
		assertThat(response.shippingAddress().addressId()).isEqualTo(101L);
		assertThat(response.shippingAddress().receiverName()).isEqualTo("선택배송지");
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
		assertThat(savedOrder.getRecipientPhone()).isEqualTo("010-2222-3333");
		assertThat(savedOrder.getZipCode()).isEqualTo("54321");
		assertThat(savedOrder.getAddress()).isEqualTo("서울시 송파구");
		assertThat(savedOrder.getDetailAddress()).isEqualTo("202동");
		assertThat(savedOrder.getDeliveryMemo()).isEqualTo("직접 입력 메모");
		assertThat(savedOrder.getFinalAmount()).isEqualTo(63000L);
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

		EventCourse course = EventCourse.builder()
			.event(event)
			.name("하프코스")
			.mapUrl("https://example.com/map")
			.distanceMeter(21097)
			.price(50000L)
			.build();
		setId(course, 10L);

		EventPace pace = EventPace.builder()
			.eventCourse(course)
			.name("05:30")
			.hour(5)
			.minutes(30)
			.capacity(100)
			.build();
		setId(pace, 20L);

		EventPackage eventPackage = EventPackage.builder()
			.event(event)
			.name("기념 티셔츠")
			.price(10000L)
			.description("기본 옵션")
			.build();
		setId(eventPackage, 30L);

		Address defaultAddress = Address.builder()
			.userId(7L)
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

	private void setId(Object target, Long id) {
		try {
			java.lang.reflect.Field field = target.getClass().getSuperclass().getDeclaredField("id");
			field.setAccessible(true);
			field.set(target, id);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
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
