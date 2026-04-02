package com.kt.onrace.domain.mypage.service.apply;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventStatus;

class ApplyDisplayStatusResolverTest {

	private final ApplyDisplayStatusResolver resolver = new ApplyDisplayStatusResolver();

	@ParameterizedTest(name = "{0}")
	@MethodSource("policyCases")
	void resolveFollowsPolicyTable(
		String description,
		ApplyDisplayStatusContext context,
		ApplyDisplayStatus expectedStatus,
		ApplyActionType expectedActionType,
		boolean expectedActionEnabled
	) {
		ApplyDisplayDecision result = resolver.resolve(context);

		assertThat(result.displayStatus()).isEqualTo(expectedStatus);
		assertThat(result.actionType()).isEqualTo(expectedActionType);
		assertThat(result.actionEnabled()).isEqualTo(expectedActionEnabled);
		assertThat(result.helperText()).isNull();
	}

	private static Stream<Arguments> policyCases() {
		return Stream.of(
			Arguments.of(
				"신청내역 + 추첨 + 진행중 + 응모완료는 응모 완료",
				context(ApplyDisplaySurface.APPLICATION_HISTORY, EventAppType.LOTTERY, EventStatus.IN_PROGRESS, ApplyUserStatus.APPLIED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.LOTTERY_APPLIED,
				ApplyActionType.NONE,
				false
			),
			Arguments.of(
				"신청내역 + 추첨 + 마감 + 응모완료는 결과 발표 대기",
				context(ApplyDisplaySurface.APPLICATION_HISTORY, EventAppType.LOTTERY, EventStatus.END, ApplyUserStatus.APPLIED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.RESULT_PENDING,
				ApplyActionType.NONE,
				false
			),
			Arguments.of(
				"신청내역 + 추첨 + 결과발표 + 응모완료는 결과 확인 필요",
				context(ApplyDisplaySurface.APPLICATION_HISTORY, EventAppType.LOTTERY, EventStatus.DRAW_COMPLETED, ApplyUserStatus.APPLIED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.RESULT_CHECK_REQUIRED,
				ApplyActionType.NONE,
				false
			),
			Arguments.of(
				"신청내역 + 추첨 + 당첨 결과는 결제 가능",
				context(ApplyDisplaySurface.APPLICATION_HISTORY, EventAppType.LOTTERY, EventStatus.DRAW_COMPLETED, ApplyUserStatus.APPLIED, ApplyResultStatus.WON),
				ApplyDisplayStatus.WON,
				ApplyActionType.CHECKOUT,
				true
			),
			Arguments.of(
				"신청내역 + 추첨 + 미당첨 결과는 읽기 상태",
				context(ApplyDisplaySurface.APPLICATION_HISTORY, EventAppType.LOTTERY, EventStatus.DRAW_COMPLETED, ApplyUserStatus.APPLIED, ApplyResultStatus.LOST),
				ApplyDisplayStatus.LOST,
				ApplyActionType.NONE,
				false
			),
			Arguments.of(
				"신청내역 + 선착 + 대기중 + 사전정보 저장은 사전정보 저장",
				context(ApplyDisplaySurface.APPLICATION_HISTORY, EventAppType.FIRST_COME, EventStatus.READY, ApplyUserStatus.PRE_SAVED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.PRE_ENTRY_SAVED,
				ApplyActionType.EDIT,
				true
			),
			Arguments.of(
				"대기목록 + 선착 + 대기중 + 사전정보 저장은 신청 대기",
				context(ApplyDisplaySurface.SUMMARY, EventAppType.FIRST_COME, EventStatus.READY, ApplyUserStatus.PRE_SAVED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.WAITING_TO_APPLY,
				ApplyActionType.EDIT,
				true
			),
			Arguments.of(
				"신청내역 + 선착 + 신청중 + 사전정보 저장은 신청 가능",
				context(ApplyDisplaySurface.APPLICATION_HISTORY, EventAppType.FIRST_COME, EventStatus.IN_PROGRESS, ApplyUserStatus.PRE_SAVED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.AVAILABLE_TO_APPLY,
				ApplyActionType.APPLY,
				true
			),
			Arguments.of(
				"대기목록 + 선착 + 마감임박 + 사전정보 저장은 신청 가능",
				context(ApplyDisplaySurface.SUMMARY, EventAppType.FIRST_COME, EventStatus.CLOSING_SOON, ApplyUserStatus.PRE_SAVED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.AVAILABLE_TO_APPLY,
				ApplyActionType.APPLY,
				true
			),
			Arguments.of(
				"신청내역 + 선착 + 마감 + 사전정보 저장은 신청 마감",
				context(ApplyDisplaySurface.APPLICATION_HISTORY, EventAppType.FIRST_COME, EventStatus.END, ApplyUserStatus.PRE_SAVED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.ENTRY_CLOSED,
				ApplyActionType.NONE,
				false
			),
			Arguments.of(
				"대기목록 + 선착 + 마감 + 사전정보 저장은 신청 불가",
				context(ApplyDisplaySurface.SUMMARY, EventAppType.FIRST_COME, EventStatus.END, ApplyUserStatus.PRE_SAVED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.ENTRY_UNAVAILABLE,
				ApplyActionType.NONE,
				false
			),
			Arguments.of(
				"대기목록 + 선착 + 예약 상태는 결제 가능",
				context(ApplyDisplaySurface.SUMMARY, EventAppType.FIRST_COME, EventStatus.IN_PROGRESS, ApplyUserStatus.RESERVED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.RESERVED,
				ApplyActionType.CHECKOUT,
				true
			),
			Arguments.of(
				"대기목록 + 선착 + 신청 완료는 읽기 상태",
				context(ApplyDisplaySurface.SUMMARY, EventAppType.FIRST_COME, EventStatus.IN_PROGRESS, ApplyUserStatus.APPLIED, ApplyResultStatus.NONE),
				ApplyDisplayStatus.ENTRY_APPLIED,
				ApplyActionType.NONE,
				false
			)
		);
	}

	private static ApplyDisplayStatusContext context(
		ApplyDisplaySurface surface,
		EventAppType appType,
		EventStatus eventStatus,
		ApplyUserStatus userStatus,
		ApplyResultStatus resultStatus
	) {
		return new ApplyDisplayStatusContext(surface, appType, eventStatus, userStatus, resultStatus);
	}
}
