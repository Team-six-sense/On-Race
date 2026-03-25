package com.kt.onrace.domain.mypage.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.redisson.api.RedissonClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;
import com.kt.onrace.domain.mypage.client.AuthAccountClient;
import com.kt.onrace.domain.entry.listener.EntryExpListener;
import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.entry.repository.EntryRepository;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.event.entity.EventRegion;
import com.kt.onrace.domain.event.entity.EventType;
import com.kt.onrace.domain.event.repository.EventCourseRepository;
import com.kt.onrace.domain.event.repository.EventPaceRepository;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.member.entity.Member;
import com.kt.onrace.domain.member.repository.MemberRepository;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.kt.onrace.domain.order.repository.OrderRepository;

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
	private EventPaceRepository eventPaceRepository;

	@Autowired
	private EventCourseRepository eventCourseRepository;

	@Autowired
	private EventRepository eventRepository;

	@MockBean
	private AuthAccountClient authAccountClient;

	@MockBean
	private RedissonClient redissonClient;

	@MockBean
	private EntryExpListener entryExpListener;

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		entryRepository.deleteAll();
		addressRepository.deleteAll();
		eventPaceRepository.deleteAll();
		eventCourseRepository.deleteAll();
		eventRepository.deleteAll();
		memberRepository.deleteAll();

		createMember(MIXED_USER_ID);
		createMember(EMPTY_USER_ID);
		createMember(ENTRY_ONLY_USER_ID);
		createMember(ORDER_ONLY_USER_ID);
		createMember(ADDRESS_ONLY_USER_ID);

		stubAuthAccount(MIXED_USER_ID, "홍길동", "01012345678", "LOCAL", "VERIFIED", true);
		stubAuthAccount(EMPTY_USER_ID, "빈사용자", "01000000000", "LOCAL", "UNVERIFIED", false);
		stubAuthAccount(ENTRY_ONLY_USER_ID, "신청사용자", "01011112222", "KAKAO", "UNVERIFIED", false);
		stubAuthAccount(ORDER_ONLY_USER_ID, "주문사용자", "01033334444", "NAVER", "VERIFIED", true);
		stubAuthAccount(ADDRESS_ONLY_USER_ID, "주소사용자", "01022223333", "LOCAL", "UNVERIFIED", false);

		LocalDateTime now = LocalDateTime.now();

		seedMixedUser(now);
		seedEntryOnlyUser(now);
		seedOrderOnlyUser(now);
		seedAddressOnlyUser();
	}

	@Test
	void callMyPageEndpointsAndVerifyActualResponseShape() throws Exception {
		JsonNode account = get(MIXED_USER_ID, "/mypage/account");
		JsonNode overview = get(MIXED_USER_ID, "/mypage");
		JsonNode entries = get(MIXED_USER_ID, "/mypage/entries");
		JsonNode waitingEntries = get(MIXED_USER_ID, "/mypage/waiting-entries");
		JsonNode orders = get(MIXED_USER_ID, "/mypage/orders");
		JsonNode orderDetail = get(MIXED_USER_ID, "/mypage/orders/ORD-TEST-0001");
		JsonNode address = get(MIXED_USER_ID, "/mypage/address");

		JsonNode emptyAccount = get(EMPTY_USER_ID, "/mypage/account");
		JsonNode emptyOverview = get(EMPTY_USER_ID, "/mypage");
		JsonNode emptyEntries = get(EMPTY_USER_ID, "/mypage/entries");
		JsonNode emptyWaitingEntries = get(EMPTY_USER_ID, "/mypage/waiting-entries");
		JsonNode emptyOrders = get(EMPTY_USER_ID, "/mypage/orders");
		JsonNode emptyAddress = get(EMPTY_USER_ID, "/mypage/address");

		assertThat(account.path("data").path("name").asText()).isEqualTo("홍길동");
		assertThat(account.path("data").path("authProvider").asText()).isEqualTo("LOCAL");
		assertThat(account.path("data").path("verificationStatus").asText()).isEqualTo("VERIFIED");
		assertThat(account.path("data").path("marketingConsent").asBoolean()).isTrue();
		assertThat(account.path("data").path("address").path("hasAddress").asBoolean()).isTrue();
		assertThat(account.path("data").path("address").path("defaultAddress").path("label").asText()).isEqualTo("집");

		assertThat(overview.path("data").path("entries").path("totalCount").asInt()).isEqualTo(1);
		assertThat(overview.path("data").path("waitingEntries").path("totalCount").asInt()).isEqualTo(1);
		assertThat(overview.path("data").path("orders").path("totalCount").asInt()).isEqualTo(1);
		assertThat(overview.path("data").path("address").path("hasAddress").asBoolean()).isTrue();

		assertThat(entries.path("data").path("items").get(0).path("status").asText()).isEqualTo("응모 완료");
		assertThat(entries.path("data").path("page").asInt()).isZero();
		assertThat(entries.path("data").path("size").asInt()).isEqualTo(20);
		assertThat(entries.path("data").path("hasNext").asBoolean()).isFalse();
		assertThat(entries.path("data").path("items").get(0).path("actionType").asText()).isEqualTo("NONE");
		assertThat(entries.path("data").path("items").get(0).path("actionLabel").isNull()).isTrue();
		assertThat(entries.path("data").path("items").get(0).path("thumbnailUrl").isNull()).isTrue();

		assertThat(waitingEntries.path("data").path("items").get(0).path("status").asText()).isEqualTo("신청 대기");
		assertThat(waitingEntries.path("data").path("page").asInt()).isZero();
		assertThat(waitingEntries.path("data").path("size").asInt()).isEqualTo(20);
		assertThat(waitingEntries.path("data").path("hasNext").asBoolean()).isFalse();
		assertThat(waitingEntries.path("data").path("items").get(0).path("actionType").asText()).isEqualTo("EDIT");
		assertThat(waitingEntries.path("data").path("items").get(0).path("actionLabel").asText()).isEqualTo("사전정보 수정");

		assertThat(orders.path("data").path("items").get(0).path("status").asText()).isEqualTo("결제 대기");
		assertThat(orders.path("data").path("page").asInt()).isZero();
		assertThat(orders.path("data").path("size").asInt()).isEqualTo(20);
		assertThat(orders.path("data").path("hasNext").asBoolean()).isFalse();
		assertThat(orders.path("data").path("items").get(0).path("actionType").asText()).isEqualTo("DETAIL");
		assertThat(orders.path("data").path("items").get(0).path("paymentDeadlineAt").isNull()).isTrue();
		assertThat(orderDetail.path("data").path("orderNumber").asText()).isEqualTo("ORD-TEST-0001");
		assertThat(orderDetail.path("data").path("status").asText()).isEqualTo("결제 대기");
		assertThat(orderDetail.path("data").path("canCancel").asBoolean()).isTrue();
		assertThat(orderDetail.path("data").path("canRefund").asBoolean()).isFalse();

		assertThat(address.path("data").path("hasAddress").asBoolean()).isTrue();
		assertThat(address.path("data").path("defaultAddress").path("label").asText()).isEqualTo("집");

		assertThat(emptyAccount.path("data").path("name").asText()).isEqualTo("빈사용자");
		assertThat(emptyAccount.path("data").path("verificationStatus").asText()).isEqualTo("UNVERIFIED");
		assertThat(emptyAccount.path("data").path("address").path("hasAddress").asBoolean()).isFalse();
		assertThat(emptyOverview.path("data").path("entries").path("items").isArray()).isTrue();
		assertThat(emptyOverview.path("data").path("entries").path("page").asInt()).isZero();
		assertThat(emptyOverview.path("data").path("entries").path("size").asInt()).isEqualTo(3);
		assertThat(emptyOverview.path("data").path("entries").path("items")).isEmpty();
		assertThat(emptyOverview.path("data").path("address").path("hasAddress").asBoolean()).isFalse();
		assertThat(emptyOverview.path("data").path("address").path("defaultAddress").isNull()).isTrue();
		assertThat(emptyEntries.path("data").path("page").asInt()).isZero();
		assertThat(emptyEntries.path("data").path("size").asInt()).isEqualTo(20);
		assertThat(emptyEntries.path("data").path("items")).isEmpty();
		assertThat(emptyWaitingEntries.path("data").path("page").asInt()).isZero();
		assertThat(emptyWaitingEntries.path("data").path("size").asInt()).isEqualTo(20);
		assertThat(emptyWaitingEntries.path("data").path("items")).isEmpty();
		assertThat(emptyOrders.path("data").path("page").asInt()).isZero();
		assertThat(emptyOrders.path("data").path("size").asInt()).isEqualTo(20);
		assertThat(emptyOrders.path("data").path("items")).isEmpty();
		assertThat(emptyAddress.path("data").path("hasAddress").asBoolean()).isFalse();
		assertThat(emptyAddress.path("data").path("defaultAddress").isNull()).isTrue();

		System.out.println("--- populated GET /mypage/account ---");
		System.out.println(pretty(account));
		System.out.println("--- populated GET /mypage ---");
		System.out.println(pretty(overview));
		System.out.println("--- populated GET /mypage/entries ---");
		System.out.println(pretty(entries));
		System.out.println("--- populated GET /mypage/waiting-entries ---");
		System.out.println(pretty(waitingEntries));
		System.out.println("--- populated GET /mypage/orders ---");
		System.out.println(pretty(orders));
		System.out.println("--- populated GET /mypage/orders/{orderNumber} ---");
		System.out.println(pretty(orderDetail));
		System.out.println("--- populated GET /mypage/address ---");
		System.out.println(pretty(address));
		System.out.println("--- empty GET /mypage/account ---");
		System.out.println(pretty(emptyAccount));
		System.out.println("--- empty GET /mypage ---");
		System.out.println(pretty(emptyOverview));
		System.out.println("--- empty GET /mypage/entries ---");
		System.out.println(pretty(emptyEntries));
		System.out.println("--- empty GET /mypage/waiting-entries ---");
		System.out.println(pretty(emptyWaitingEntries));
		System.out.println("--- empty GET /mypage/orders ---");
		System.out.println(pretty(emptyOrders));
		System.out.println("--- empty GET /mypage/address ---");
		System.out.println(pretty(emptyAddress));
	}

	@Test
	void prepareRequestedSeedSetsAndVerifyApiResponses() throws Exception {
		JsonNode entryAccount = get(ENTRY_ONLY_USER_ID, "/mypage/account");
		JsonNode entryOverview = get(ENTRY_ONLY_USER_ID, "/mypage");
		JsonNode entryItems = get(ENTRY_ONLY_USER_ID, "/mypage/entries");
		JsonNode waitingItems = get(ENTRY_ONLY_USER_ID, "/mypage/waiting-entries");
		JsonNode entryOrders = get(ENTRY_ONLY_USER_ID, "/mypage/orders");
		JsonNode entryAddress = get(ENTRY_ONLY_USER_ID, "/mypage/address");

		assertThat(entryAccount.path("data").path("authProvider").asText()).isEqualTo("KAKAO");
		assertThat(entryOverview.path("data").path("entries").path("totalCount").asInt()).isEqualTo(3);
		assertThat(entryOverview.path("data").path("waitingEntries").path("totalCount").asInt()).isEqualTo(1);
		assertThat(entryOverview.path("data").path("orders").path("totalCount").asInt()).isZero();
		assertThat(entryOverview.path("data").path("address").path("hasAddress").asBoolean()).isFalse();
		assertThat(entryItems.path("data").path("page").asInt()).isZero();
		assertThat(entryItems.path("data").path("size").asInt()).isEqualTo(20);
		assertThat(textValues(entryItems.path("data").path("items"), "status"))
			.containsExactlyInAnyOrder("응모 완료", "당첨", "미당첨");
		assertThat(textValues(entryItems.path("data").path("items"), "actionType"))
			.containsExactlyInAnyOrder("NONE", "CHECKOUT", "NONE");
		assertThat(textValues(waitingItems.path("data").path("items"), "status"))
			.containsExactly("신청 대기");
		assertThat(entryOrders.path("data").path("items")).isEmpty();
		assertThat(entryAddress.path("data").path("hasAddress").asBoolean()).isFalse();

		JsonNode orderAccount = get(ORDER_ONLY_USER_ID, "/mypage/account");
		JsonNode orderOverview = get(ORDER_ONLY_USER_ID, "/mypage");
		JsonNode orderItems = get(ORDER_ONLY_USER_ID, "/mypage/orders");
		JsonNode orderEntries = get(ORDER_ONLY_USER_ID, "/mypage/entries");
		JsonNode orderWaitingEntries = get(ORDER_ONLY_USER_ID, "/mypage/waiting-entries");
		JsonNode orderAddress = get(ORDER_ONLY_USER_ID, "/mypage/address");

		assertThat(orderAccount.path("data").path("authProvider").asText()).isEqualTo("NAVER");
		assertThat(orderOverview.path("data").path("entries").path("totalCount").asInt()).isZero();
		assertThat(orderOverview.path("data").path("waitingEntries").path("totalCount").asInt()).isZero();
		assertThat(orderOverview.path("data").path("orders").path("totalCount").asInt()).isEqualTo(3);
		assertThat(orderItems.path("data").path("page").asInt()).isZero();
		assertThat(orderItems.path("data").path("size").asInt()).isEqualTo(20);
		assertThat(textValues(orderItems.path("data").path("items"), "status"))
			.containsExactlyInAnyOrder("결제 대기", "결제 완료", "주문 취소");
		assertThat(textValues(orderItems.path("data").path("items"), "actionType"))
			.containsExactlyInAnyOrder("DETAIL", "DETAIL", "DETAIL");
		assertThat(orderEntries.path("data").path("items")).isEmpty();
		assertThat(orderWaitingEntries.path("data").path("items")).isEmpty();
		assertThat(orderAddress.path("data").path("hasAddress").asBoolean()).isFalse();

		JsonNode addressAccount = get(ADDRESS_ONLY_USER_ID, "/mypage/account");
		JsonNode addressOverview = get(ADDRESS_ONLY_USER_ID, "/mypage");
		JsonNode addressOnly = get(ADDRESS_ONLY_USER_ID, "/mypage/address");
		JsonNode addressEntries = get(ADDRESS_ONLY_USER_ID, "/mypage/entries");
		JsonNode addressOrders = get(ADDRESS_ONLY_USER_ID, "/mypage/orders");

		assertThat(addressAccount.path("data").path("name").asText()).isEqualTo("주소사용자");
		assertThat(addressOverview.path("data").path("entries").path("totalCount").asInt()).isZero();
		assertThat(addressOverview.path("data").path("waitingEntries").path("totalCount").asInt()).isZero();
		assertThat(addressOverview.path("data").path("orders").path("totalCount").asInt()).isZero();
		assertThat(addressOverview.path("data").path("address").path("hasAddress").asBoolean()).isTrue();
		assertThat(addressOnly.path("data").path("defaultAddress").path("label").asText()).isEqualTo("집");
		assertThat(addressOnly.path("data").path("defaultAddress").path("isDefault").asBoolean()).isTrue();
		assertThat(addressRepository.countByUserId(ADDRESS_ONLY_USER_ID)).isEqualTo(2L);
		assertThat(addressEntries.path("data").path("items")).isEmpty();
		assertThat(addressOrders.path("data").path("items")).isEmpty();
	}

	private JsonNode get(Long userId, String path) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-User-Id", userId.toString());
		headers.add("X-Gateway-Token", GATEWAY_TOKEN);

		ResponseEntity<String> response = restTemplate.exchange(
			"http://localhost:" + port + path,
			HttpMethod.GET,
			new HttpEntity<>(headers),
			String.class
		);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		return objectMapper.readTree(response.getBody());
	}

	private String pretty(JsonNode body) throws Exception {
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
	}

	private List<String> textValues(JsonNode items, String fieldName) {
		List<String> values = new ArrayList<>();
		for (JsonNode item : items) {
			values.add(item.path(fieldName).asText());
		}
		return values;
	}

	private void stubAuthAccount(
		Long userId,
		String name,
		String phone,
		String authProvider,
		String verificationStatus,
		boolean marketingConsent
	) {
		given(authAccountClient.getMyInfo(userId)).willReturn(
			new AuthAccountClient.AccountSummary(name, phone, authProvider, verificationStatus, marketingConsent)
		);
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

		EventCourse savedCourse = eventCourseRepository.saveAndFlush(EventCourse.builder()
			.event(savedEvent)
			.name(courseName)
			.mapUrl(null)
			.distanceMeter(distanceMeter)
			.timeLimit(180)
			.waterSource(10)
			.altitude(100)
			.courseRoute(courseName + " 코스")
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
