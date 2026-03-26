package com.kt.onrace.domain.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventRegion;
import com.kt.onrace.domain.event.entity.EventType;
import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;

class MyPageDisplayStatusResolverTest {

	private final MyPageDisplayStatusResolver resolver = new MyPageDisplayStatusResolver();

	@ParameterizedTest(name = "{0}")
	@MethodSource("applicationHistoryCases")
	void resolveApplicationHistoryStatusFollowsPolicy(
		String description,
		Event event,
		Entry entry,
		String expectedStatus,
		String expectedActionType,
		String expectedActionLabel,
		boolean expectedActionEnabled
	) {
		MyPageStatusDto result = resolver.resolveApplicationHistoryStatus(event, entry);

		assertThat(result.statusText()).isEqualTo(expectedStatus);
		assertThat(result.actionType()).isEqualTo(expectedActionType);
		assertThat(result.actionLabel()).isEqualTo(expectedActionLabel);
		assertThat(result.actionEnabled()).isEqualTo(expectedActionEnabled);
	}

	private static Stream<Arguments> applicationHistoryCases() {
		LocalDateTime now = LocalDateTime.now();

		return Stream.of(
			Arguments.of(
				"추첨 + 진행중 + 응모완료는 응모 완료",
				createEvent(EventAppType.LOTTERY, now.minusDays(5), now.plusDays(5), now.plusDays(10), false),
				createEntry(EntryStatus.APPLIED),
				"응모 완료",
				"NONE",
				null,
				false
			),
			Arguments.of(
				"추첨 + 마감 + 응모완료는 결과 발표 대기",
				createEvent(EventAppType.LOTTERY, now.minusDays(10), now.minusDays(1), now.plusDays(2), false),
				createEntry(EntryStatus.APPLIED),
				"결과 발표 대기",
				"NONE",
				null,
				false
			),
			Arguments.of(
				"추첨 + 결과발표 + 응모완료는 결과 확인 필요",
				createEvent(EventAppType.LOTTERY, now.minusDays(10), now.minusDays(3), now.minusHours(1), false),
				createEntry(EntryStatus.APPLIED),
				"결과 확인 필요",
				"NONE",
				null,
				false
			),
			Arguments.of(
				"추첨 + 당첨은 결제하기 액션",
				createEvent(EventAppType.LOTTERY, now.minusDays(10), now.minusDays(3), now.minusHours(1), false),
				createEntry(EntryStatus.WON),
				"당첨",
				"CHECKOUT",
				"결제하기",
				true
			),
			Arguments.of(
				"추첨 + 미당첨은 액션 없음",
				createEvent(EventAppType.LOTTERY, now.minusDays(10), now.minusDays(3), now.minusHours(1), false),
				createEntry(EntryStatus.LOST),
				"미당첨",
				"NONE",
				null,
				false
			),
			Arguments.of(
				"선착 + 대기중 + 사전정보 저장은 수정 가능",
				createEvent(EventAppType.FIRST_COME, now.plusDays(2), now.plusDays(7), null, false),
				createEntry(EntryStatus.PRE_SAVED),
				"사전정보 저장",
				"EDIT",
				"사전정보 수정",
				true
			),
			Arguments.of(
				"선착 + 신청중 + 사전정보 저장은 신청 가능",
				createEvent(EventAppType.FIRST_COME, now.minusDays(1), now.plusDays(5), null, false),
				createEntry(EntryStatus.PRE_SAVED),
				"신청 가능",
				"APPLY",
				"신청하기",
				true
			),
			Arguments.of(
				"선착 + 마감 + 사전정보 저장은 신청 마감",
				createEvent(EventAppType.FIRST_COME, now.minusDays(5), now.minusHours(1), null, false),
				createEntry(EntryStatus.PRE_SAVED),
				"신청 마감",
				"NONE",
				null,
				false
			),
			Arguments.of(
				"선착 + 예약 상태는 결제하기 액션",
				createEvent(EventAppType.FIRST_COME, now.minusDays(1), now.plusDays(5), null, false),
				createEntry(EntryStatus.RESERVED),
				"예약 중",
				"CHECKOUT",
				"결제하기",
				true
			),
			Arguments.of(
				"선착 + 신청 완료는 읽기 상태",
				createEvent(EventAppType.FIRST_COME, now.minusDays(1), now.plusDays(5), null, false),
				createEntry(EntryStatus.APPLIED),
				"신청 완료",
				"NONE",
				null,
				false
			)
		);
	}

	private static Event createEvent(
		EventAppType appType,
		LocalDateTime appStartAt,
		LocalDateTime appEndAt,
		LocalDateTime lotteryAnnouncedAt,
		boolean soldOut
	) {
		return Event.builder()
			.title("테스트 이벤트")
			.type(EventType.RUNNING)
			.appType(appType)
			.eventAt(appEndAt.plusDays(10))
			.appStartAt(appStartAt)
			.appEndAt(appEndAt)
			.region(EventRegion.SEOUL)
			.venue("올림픽공원")
			.lotteryAnnouncedAt(lotteryAnnouncedAt)
			.notice("테스트")
			.isView(true)
			.soldOut(soldOut)
			.build();
	}

	private static Entry createEntry(EntryStatus status) {
		return Entry.builder()
			.userId(1L)
			.status(status)
			.build();
	}
}
