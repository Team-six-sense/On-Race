package com.kt.onrace.domain.mypage.service.apply;

import org.springframework.stereotype.Component;

import com.kt.onrace.domain.event.entity.EventStatus;

/**
 * 신청내역 화면 표시 상태와 액션을 정책표 기준으로 해석한다.
 */
@Component
public class ApplyDisplayStatusResolver {

	public ApplyDisplayDecision resolve(ApplyDisplayStatusContext context) {
		if (context.resultStatus() == ApplyResultStatus.WON) {
			return ApplyDisplayDecision.of(ApplyDisplayStatus.WON, ApplyActionType.CHECKOUT);
		}

		if (context.resultStatus() == ApplyResultStatus.LOST) {
			return ApplyDisplayDecision.of(ApplyDisplayStatus.LOST, ApplyActionType.NONE);
		}

		return switch (context.appType()) {
			case LOTTERY -> resolveLottery(context);
			case FIRST_COME -> resolveFirstCome(context);
		};
	}

	private ApplyDisplayDecision resolveLottery(ApplyDisplayStatusContext context) {
		if (context.userStatus() != ApplyUserStatus.APPLIED) {
			return fallbackDecision(context);
		}

		return switch (context.eventStatus()) {
			case END -> ApplyDisplayDecision.of(ApplyDisplayStatus.RESULT_PENDING, ApplyActionType.NONE);
			case DRAW_COMPLETED -> ApplyDisplayDecision.of(ApplyDisplayStatus.RESULT_CHECK_REQUIRED, ApplyActionType.NONE);
			case READY, IN_PROGRESS, CLOSING_SOON -> ApplyDisplayDecision.of(
				ApplyDisplayStatus.LOTTERY_APPLIED,
				ApplyActionType.NONE
			);
		};
	}

	private ApplyDisplayDecision resolveFirstCome(ApplyDisplayStatusContext context) {
		if (context.userStatus() == ApplyUserStatus.RESERVED) {
			return ApplyDisplayDecision.of(ApplyDisplayStatus.RESERVED, ApplyActionType.CHECKOUT);
		}

		if (context.userStatus() == ApplyUserStatus.APPLIED) {
			return ApplyDisplayDecision.of(ApplyDisplayStatus.ENTRY_APPLIED, ApplyActionType.NONE);
		}

		if (isOpenEvent(context.eventStatus())) {
			return ApplyDisplayDecision.of(ApplyDisplayStatus.AVAILABLE_TO_APPLY, ApplyActionType.APPLY);
		}

		if (isClosedEvent(context.eventStatus())) {
			return switch (context.surface()) {
				case APPLICATION_HISTORY -> ApplyDisplayDecision.of(ApplyDisplayStatus.ENTRY_CLOSED, ApplyActionType.NONE);
				case SUMMARY -> ApplyDisplayDecision.of(ApplyDisplayStatus.ENTRY_UNAVAILABLE, ApplyActionType.NONE);
			};
		}

		return switch (context.surface()) {
			case APPLICATION_HISTORY -> ApplyDisplayDecision.of(ApplyDisplayStatus.PRE_ENTRY_SAVED, ApplyActionType.EDIT);
			case SUMMARY -> ApplyDisplayDecision.of(ApplyDisplayStatus.WAITING_TO_APPLY, ApplyActionType.EDIT);
		};
	}

	private ApplyDisplayDecision fallbackDecision(ApplyDisplayStatusContext context) {
		return switch (context.userStatus()) {
			case PRE_SAVED -> switch (context.surface()) {
				case APPLICATION_HISTORY -> ApplyDisplayDecision.of(ApplyDisplayStatus.PRE_ENTRY_SAVED, ApplyActionType.EDIT);
				case SUMMARY -> ApplyDisplayDecision.of(ApplyDisplayStatus.WAITING_TO_APPLY, ApplyActionType.EDIT);
			};
			case RESERVED -> ApplyDisplayDecision.of(ApplyDisplayStatus.RESERVED, ApplyActionType.CHECKOUT);
			case APPLIED -> switch (context.appType()) {
				case LOTTERY -> ApplyDisplayDecision.of(ApplyDisplayStatus.LOTTERY_APPLIED, ApplyActionType.NONE);
				case FIRST_COME -> ApplyDisplayDecision.of(ApplyDisplayStatus.ENTRY_APPLIED, ApplyActionType.NONE);
			};
		};
	}

	private boolean isOpenEvent(EventStatus eventStatus) {
		return eventStatus == EventStatus.IN_PROGRESS || eventStatus == EventStatus.CLOSING_SOON;
	}

	private boolean isClosedEvent(EventStatus eventStatus) {
		return eventStatus == EventStatus.END || eventStatus == EventStatus.DRAW_COMPLETED;
	}
}
