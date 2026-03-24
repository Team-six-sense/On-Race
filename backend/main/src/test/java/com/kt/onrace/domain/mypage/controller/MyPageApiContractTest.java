package com.kt.onrace.domain.mypage.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;
import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.entry.listener.EntryExpListener;
import com.kt.onrace.domain.entry.repository.EntryRepository;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventImage;
import com.kt.onrace.domain.event.entity.EventImageType;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.event.entity.EventRegion;
import com.kt.onrace.domain.event.entity.EventType;
import com.kt.onrace.domain.event.repository.EventCourseRepository;
import com.kt.onrace.domain.event.repository.EventImageRepository;
import com.kt.onrace.domain.event.repository.EventPaceRepository;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.member.entity.Member;
import com.kt.onrace.domain.member.repository.MemberRepository;
import com.kt.onrace.domain.mypage.client.AuthClient;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.kt.onrace.domain.order.repository.OrderRepository;
import org.redisson.api.RedissonClient;

@ActiveProfiles("test")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = {
		"spring.datasource.url=jdbc:h2:mem:mypage-contract;MODE=MYSQL;NON_KEYWORDS=HOUR",
		"aws.s3.bucket=test-bucket",
		"aws.s3.presign-expire-seconds=900",
		"spring.autoconfigure.exclude="
			+ "org.redisson.spring.starter.RedissonAutoConfigurationV2"
	}
)
class MyPageApiContractTest {

	private static final long MIXED_USER_ID = 7L;
	private static final long EMPTY_USER_ID = 8L;
	private static final long ENTRY_ONLY_USER_ID = 11L;
	private static final long ORDER_ONLY_USER_ID = 12L;
	private static final long ADDRESS_ONLY_USER_ID = 13L;
	private static final String GATEWAY_TOKEN = "onrace-super-secret-key";

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private EntryRepository entryRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private EventImageRepository eventImageRepository;

	@Autowired
	private EventPaceRepository eventPaceRepository;

	@Autowired
	private EventCourseRepository eventCourseRepository;

	@Autowired
	private EventRepository eventRepository;

	@MockBean
	private RedissonClient redissonClient;

	@MockBean
	private EntryExpListener entryExpListener;

	@MockBean
	private AuthClient authClient;

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		entryRepository.deleteAll();
		addressRepository.deleteAll();
		eventImageRepository.deleteAll();
		eventPaceRepository.deleteAll();
		eventCourseRepository.deleteAll();
		eventRepository.deleteAll();
		memberRepository.deleteAll();

		createMember(MIXED_USER_ID);
		createMember(EMPTY_USER_ID);
		createMember(ENTRY_ONLY_USER_ID);
		createMember(ORDER_ONLY_USER_ID);
		createMember(ADDRESS_ONLY_USER_ID);

		LocalDateTime now = LocalDateTime.now();

		seedMixedUser(now);
		seedEntryOnlyUser(now);
		seedOrderOnlyUser(now);
		seedAddressOnlyUser();
	}

	@Test
	void entriesExposeReducedScopeFrontendReadyContract() throws Exception {
		JsonNode overview = get(MIXED_USER_ID, "/mypage");
		JsonNode entries = get(MIXED_USER_ID, "/mypage/entries");
		JsonNode address = get(ADDRESS_ONLY_USER_ID, "/mypage/address");
		JsonNode addressOnlyEntries = get(ADDRESS_ONLY_USER_ID, "/mypage/entries");

		assertThat(overview.path("data").path("entries").path("totalCount").asInt()).isEqualTo(1);
		assertThat(overview.path("data").path("waitingEntries").path("totalCount").asInt()).isEqualTo(1);
		assertThat(overview.path("data").path("orders").path("totalCount").asInt()).isEqualTo(2);

		assertThat(entries.path("data").path("filter").asText()).isEqualTo("ALL");
		assertThat(entries.path("data").path("empty").asBoolean()).isFalse();
		assertThat(entries.path("data").path("pagination").path("page").asInt()).isZero();
		assertThat(entries.path("data").path("pagination").path("size").asInt()).isEqualTo(20);
		assertThat(entries.path("data").path("pagination").path("totalCount").asInt()).isEqualTo(2);
		assertThat(entries.path("data").path("pagination").path("hasNext").asBoolean()).isFalse();
		assertThat(textValues(entries.path("data").path("items"), "eventName"))
			.containsExactlyInAnyOrder("서울 마라톤 대회 2026", "부산 러닝 페스티벌");
		assertThat(textValues(entries.path("data").path("items"), "applicationType"))
			.containsExactlyInAnyOrder("LOTTERY", "FIRST_COME");

		for (JsonNode item : entries.path("data").path("items")) {
			assertThat(item.path("entryId").asLong()).isPositive();
			assertThat(item.path("eventId").asLong()).isPositive();
			assertThat(item.path("displayStatus").asText()).isNotBlank();
			assertThat(item.path("deepLink").asText()).startsWith("/ticketing/");
			assertThat(item.path("thumbnailUrl").asText()).startsWith("https://example.com/events/");
		}

		JsonNode appliedItem = findItem(entries.path("data").path("items"), "eventName", "서울 마라톤 대회 2026");
		assertThat(appliedItem.path("displayStatus").asText()).isEqualTo("응모 완료");
		assertThat(appliedItem.path("actionType").asText()).isEqualTo("NONE");

		assertThat(address.path("data").path("hasAddress").asBoolean()).isTrue();
		assertThat(addressOnlyEntries.path("data").path("empty").asBoolean()).isTrue();
		assertThat(addressOnlyEntries.path("data").path("pagination").path("totalCount").asInt()).isZero();
	}

	@Test
	void accountReturnsReducedScopeResponse() throws Exception {
		org.mockito.BDDMockito.given(authClient.getAccount(MIXED_USER_ID))
			.willReturn(new AuthClient.AuthAccountResponse(
				MIXED_USER_ID,
				"mixed@test.com",
				"혼합유저",
				"01012345678",
				true,
				"PENDING",
				true
			));

		JsonNode account = get(MIXED_USER_ID, "/mypage/account");

		assertThat(account.path("data").path("name").asText()).isEqualTo("혼합유저");
		assertThat(account.path("data").path("email").asText()).isEqualTo("mixed@test.com");
		assertThat(account.path("data").path("phone").asText()).isEqualTo("01012345678");
		assertThat(account.path("data").path("canChangePassword").asBoolean()).isTrue();
		assertThat(account.path("data").path("verificationStatus").asText()).isEqualTo("PENDING");
		assertThat(account.path("data").path("marketingConsent").asBoolean()).isTrue();
	}

	@Test
	void entriesSupportBackendFilterAndPagination() throws Exception {
		JsonNode lotteryEntries = get(ENTRY_ONLY_USER_ID, "/mypage/entries?filter=LOTTERY&page=0&size=2");
		JsonNode firstComeEntries = get(ENTRY_ONLY_USER_ID, "/mypage/entries?filter=FIRST_COME&page=0&size=20");

		assertThat(lotteryEntries.path("data").path("filter").asText()).isEqualTo("LOTTERY");
		assertThat(lotteryEntries.path("data").path("pagination").path("page").asInt()).isZero();
		assertThat(lotteryEntries.path("data").path("pagination").path("size").asInt()).isEqualTo(2);
		assertThat(lotteryEntries.path("data").path("pagination").path("totalCount").asInt()).isEqualTo(3);
		assertThat(lotteryEntries.path("data").path("pagination").path("hasNext").asBoolean()).isTrue();
		assertThat(textValues(lotteryEntries.path("data").path("items"), "applicationType"))
			.containsOnly("LOTTERY");

		assertThat(firstComeEntries.path("data").path("filter").asText()).isEqualTo("FIRST_COME");
		assertThat(firstComeEntries.path("data").path("pagination").path("totalCount").asInt()).isEqualTo(2);
		assertThat(firstComeEntries.path("data").path("pagination").path("hasNext").asBoolean()).isFalse();
		assertThat(textValues(firstComeEntries.path("data").path("items"), "applicationType"))
			.containsOnly("FIRST_COME");
	}

	@Test
	void entriesHideCheckoutActionsForReducedScope() throws Exception {
		JsonNode entries = get(ENTRY_ONLY_USER_ID, "/mypage/entries");
		JsonNode waitingEntries = get(ENTRY_ONLY_USER_ID, "/mypage/waiting-entries");

		assertThat(entries.path("data").path("pagination").path("totalCount").asInt()).isEqualTo(5);
		assertThat(textValues(entries.path("data").path("items"), "actionType")).doesNotContain("CHECKOUT");

		JsonNode wonItem = findItem(entries.path("data").path("items"), "eventName", "세트1 당첨 이벤트");
		assertThat(wonItem.path("displayStatus").asText()).isEqualTo("당첨");
		assertThat(wonItem.path("actionType").asText()).isEqualTo("NONE");
		assertThat(wonItem.path("actionLabel").isNull()).isTrue();

		JsonNode reservedItem = findItem(entries.path("data").path("items"), "eventName", "세트1 예약 이벤트");
		assertThat(reservedItem.path("displayStatus").asText()).isEqualTo("예약 중");
		assertThat(reservedItem.path("actionType").asText()).isEqualTo("NONE");
		assertThat(reservedItem.path("actionLabel").isNull()).isTrue();

		assertThat(waitingEntries.path("data").path("totalCount").asInt()).isEqualTo(2);
		assertThat(textValues(waitingEntries.path("data").path("items"), "status"))
			.containsExactlyInAnyOrder("신청 대기", "예약 중");
	}

	@Test
	void entriesKeepEmptySuccessContractAndExistingErrorEnvelope() throws Exception {
		JsonNode emptyEntries = get(EMPTY_USER_ID, "/mypage/entries?filter=ALL&page=0&size=20");
		ResponseEntity<String> invalidFilterResponse = exchange(EMPTY_USER_ID, "/mypage/entries?filter=INVALID");
		JsonNode invalidFilterBody = objectMapper.readTree(invalidFilterResponse.getBody());

		assertThat(emptyEntries.path("data").path("filter").asText()).isEqualTo("ALL");
		assertThat(emptyEntries.path("data").path("empty").asBoolean()).isTrue();
		assertThat(emptyEntries.path("data").path("pagination").path("page").asInt()).isZero();
		assertThat(emptyEntries.path("data").path("pagination").path("size").asInt()).isEqualTo(20);
		assertThat(emptyEntries.path("data").path("pagination").path("totalCount").asInt()).isZero();
		assertThat(emptyEntries.path("data").path("items")).isEmpty();

		assertThat(invalidFilterResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(invalidFilterBody.path("success").asBoolean()).isFalse();
		assertThat(invalidFilterBody.path("code").asText())
			.isEqualTo(BusinessErrorCode.COMMON_INVALID_PARAMETER.getCode());
		assertThat(invalidFilterBody.path("message").asText())
			.isEqualTo(BusinessErrorCode.COMMON_INVALID_PARAMETER.getMessage());
	}

	private ResponseEntity<String> exchange(Long userId, String path) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-User-Id", userId.toString());
		headers.add("X-Gateway-Token", GATEWAY_TOKEN);

		return restTemplate.exchange(
			"http://localhost:" + port + path,
			HttpMethod.GET,
			new HttpEntity<>(headers),
			String.class
		);
	}

	private JsonNode get(Long userId, String path) throws Exception {
		ResponseEntity<String> response = exchange(userId, path);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		return objectMapper.readTree(response.getBody());
	}

	private List<String> textValues(JsonNode items, String fieldName) {
		List<String> values = new ArrayList<>();
		for (JsonNode item : items) {
			values.add(item.path(fieldName).asText());
		}
		return values;
	}

	private JsonNode findItem(JsonNode items, String fieldName, String expectedValue) {
		for (JsonNode item : items) {
			if (expectedValue.equals(item.path(fieldName).asText())) {
				return item;
			}
		}
		throw new AssertionError("Item not found: " + expectedValue);
	}

	private void createMember(Long userId) {
		memberRepository.saveAndFlush(Member.createMember(userId));
	}

	private void seedMixedUser(LocalDateTime now) {
		createAddress(MIXED_USER_ID, "집", true, "홍길동", "01012345678", "12345", "서울시 강남구", "101동", "문앞");

		EventBundle appliedBundle = createEventBundle(
			"서울 마라톤 대회 2026",
			EventType.MARATHON,
			EventAppType.LOTTERY,
			now.plusDays(30),
			now.minusDays(3),
			now.plusDays(5),
			now.plusDays(7),
			EventRegion.SEOUL,
			"올림픽공원",
			"풀코스",
			42195,
			35000L,
			"6:00/km",
			6,
			0,
			300
		);
		createEntry(MIXED_USER_ID, appliedBundle, EntryStatus.APPLIED);

		EventBundle waitingBundle = createEventBundle(
			"부산 러닝 페스티벌",
			EventType.RUNNING,
			EventAppType.FIRST_COME,
			now.plusDays(40),
			now.plusDays(3),
			now.plusDays(10),
			null,
			EventRegion.BUSAN,
			"해운대",
			"하프",
			21097,
			28000L,
			"7:00/km",
			7,
			0,
			150
		);
		createEntry(MIXED_USER_ID, waitingBundle, EntryStatus.PRE_SAVED);

		createOrder(MIXED_USER_ID, appliedBundle, OrderStatus.PENDING, "ORD-TEST-0001");
		createOrder(MIXED_USER_ID, appliedBundle, OrderStatus.PAID, "ORD-TEST-0002");
	}

	private void seedEntryOnlyUser(LocalDateTime now) {
		EventBundle preSavedBundle = createEventBundle(
			"세트1 신청 대기 이벤트",
			EventType.RUNNING,
			EventAppType.FIRST_COME,
			now.plusDays(14),
			now.plusDays(2),
			now.plusDays(5),
			null,
			EventRegion.SEOUL,
			"잠실종합운동장",
			"10K",
			10000,
			15000L,
			"5:30/km",
			5,
			30,
			200
		);
		createEntry(ENTRY_ONLY_USER_ID, preSavedBundle, EntryStatus.PRE_SAVED);

		EventBundle reservedBundle = createEventBundle(
			"세트1 예약 이벤트",
			EventType.RUNNING,
			EventAppType.FIRST_COME,
			now.plusDays(16),
			now.minusDays(1),
			now.plusDays(1),
			null,
			EventRegion.SEOUL,
			"잠실보조경기장",
			"5K",
			5000,
			12000L,
			"6:30/km",
			6,
			30,
			100
		);
		createEntry(ENTRY_ONLY_USER_ID, reservedBundle, EntryStatus.RESERVED);

		EventBundle appliedBundle = createEventBundle(
			"세트1 응모 완료 이벤트",
			EventType.MARATHON,
			EventAppType.LOTTERY,
			now.plusDays(25),
			now.minusDays(2),
			now.plusDays(4),
			now.plusDays(8),
			EventRegion.SEOUL,
			"여의도공원",
			"하프",
			21097,
			29000L,
			"6:10/km",
			6,
			10,
			180
		);
		createEntry(ENTRY_ONLY_USER_ID, appliedBundle, EntryStatus.APPLIED);

		EventBundle wonBundle = createEventBundle(
			"세트1 당첨 이벤트",
			EventType.MARATHON,
			EventAppType.LOTTERY,
			now.plusDays(32),
			now.minusDays(10),
			now.minusDays(5),
			now.minusDays(1),
			EventRegion.SEOUL,
			"서울광장",
			"풀코스",
			42195,
			39000L,
			"6:20/km",
			6,
			20,
			160
		);
		createEntry(ENTRY_ONLY_USER_ID, wonBundle, EntryStatus.WON);

		EventBundle lostBundle = createEventBundle(
			"세트1 미당첨 이벤트",
			EventType.MARATHON,
			EventAppType.LOTTERY,
			now.plusDays(36),
			now.minusDays(11),
			now.minusDays(6),
			now.minusDays(2),
			EventRegion.BUSAN,
			"벡스코",
			"10K",
			10000,
			22000L,
			"6:40/km",
			6,
			40,
			140
		);
		createEntry(ENTRY_ONLY_USER_ID, lostBundle, EntryStatus.LOST);
	}

	private void seedOrderOnlyUser(LocalDateTime now) {
		EventBundle orderBundle = createEventBundle(
			"세트2 주문 전용 이벤트",
			EventType.RUNNING,
			EventAppType.FIRST_COME,
			now.plusDays(20),
			now.minusDays(3),
			now.plusDays(3),
			null,
			EventRegion.SEOUL,
			"상암월드컵경기장",
			"15K",
			15000,
			33000L,
			"5:50/km",
			5,
			50,
			120
		);
		createOrder(ORDER_ONLY_USER_ID, orderBundle, OrderStatus.PENDING, "ORD-SET2-PENDING");
		createOrder(ORDER_ONLY_USER_ID, orderBundle, OrderStatus.PAID, "ORD-SET2-PAID");
		createOrder(ORDER_ONLY_USER_ID, orderBundle, OrderStatus.CANCELLED, "ORD-SET2-CANCELLED");
	}

	private void seedAddressOnlyUser() {
		createAddress(ADDRESS_ONLY_USER_ID, "집", true, "주소사용자", "01022223333", "06236", "서울시 강남구", "201동", "경비실");
		createAddress(ADDRESS_ONLY_USER_ID, "회사", false, "주소사용자", "01022223333", "04799", "서울시 성동구", "8층", "리셉션");
	}

	private Address createAddress(Long userId, String label, boolean isDefault, String receiverName, String phone,
		String zipcode, String address1, String address2, String memo) {
		return addressRepository.saveAndFlush(Address.builder()
			.userId(userId)
			.label(label)
			.normalizedLabel(label)
			.receiverName(receiverName)
			.phone(phone)
			.zipcode(zipcode)
			.address1(address1)
			.address2(address2)
			.memo(memo)
			.isDefault(isDefault)
			.activeDefaultOwnerId(isDefault ? userId : null)
			.build());
	}

	private Entry createEntry(Long userId, EventBundle bundle, EntryStatus status) {
		return entryRepository.saveAndFlush(Entry.builder()
			.userId(userId)
			.event(bundle.event())
			.eventCourse(bundle.course())
			.eventPace(bundle.pace())
			.status(status)
			.build());
	}

	private Order createOrder(Long userId, EventBundle bundle, OrderStatus orderStatus, String orderNumber) {
		long price = bundle.course().getPrice();
		long shippingFee = 3000L;

		return orderRepository.saveAndFlush(Order.builder()
			.orderNumber(orderNumber)
			.userId(userId)
			.eventCourseId(bundle.course().getId())
			.eventPaceId(bundle.pace().getId())
			.orderStatus(orderStatus)
			.itemTotalAmount(price)
			.shippingFee(shippingFee)
			.discountAmount(0L)
			.finalAmount(price + shippingFee)
			.recipientName("테스트 사용자")
			.addressLabel("기본 배송지")
			.recipientPhone("01099998888")
			.zipCode("12345")
			.address("서울시 테스트구")
			.detailAddress("999동")
			.deliveryMemo("문앞")
			.build());
	}

	private EventBundle createEventBundle(
		String title,
		EventType eventType,
		EventAppType appType,
		LocalDateTime eventAt,
		LocalDateTime appStartAt,
		LocalDateTime appEndAt,
		LocalDateTime lotteryAnnouncedAt,
		EventRegion region,
		String venue,
		String courseName,
		int distanceMeter,
		long price,
		String paceName,
		int hour,
		int minutes,
		int capacity
	) {
		Event savedEvent = eventRepository.saveAndFlush(Event.builder()
			.title(title)
			.type(eventType)
			.appType(appType)
			.eventAt(eventAt)
			.appStartAt(appStartAt)
			.appEndAt(appEndAt)
			.region(region)
			.venue(venue)
			.lotteryAnnouncedAt(lotteryAnnouncedAt)
			.notice(title + " 안내")
			.isView(true)
			.soldOut(false)
			.build());

		eventImageRepository.saveAndFlush(EventImage.builder()
			.event(savedEvent)
			.type(EventImageType.THUMBNAIL)
			.url("https://example.com/events/" + savedEvent.getId() + "-thumb.png")
			.sort(0)
			.build());

		EventCourse savedCourse = eventCourseRepository.saveAndFlush(EventCourse.builder()
			.event(savedEvent)
			.name(courseName)
			.mapUrl(null)
			.distanceMeter(distanceMeter)
			.price(price)
			.build());

		EventPace savedPace = eventPaceRepository.saveAndFlush(EventPace.builder()
			.eventCourse(savedCourse)
			.name(paceName)
			.hour(hour)
			.minutes(minutes)
			.capacity(capacity)
			.build());

		return new EventBundle(savedEvent, savedCourse, savedPace);
	}

	private record EventBundle(
		Event event,
		EventCourse course,
		EventPace pace
	) {
	}
}
