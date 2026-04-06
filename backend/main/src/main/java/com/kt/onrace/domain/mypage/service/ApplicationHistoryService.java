package com.kt.onrace.domain.mypage.service;

import static com.kt.onrace.domain.entry.entity.QEntry.entry;
import static com.kt.onrace.domain.event.entity.QEvent.event;
import static com.kt.onrace.domain.event.entity.QEventCourse.eventCourse;
import static com.kt.onrace.domain.event.entity.QEventPace.eventPace;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryFilter;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;
import com.kt.onrace.domain.mypage.service.apply.ApplyDisplayDecision;
import com.kt.onrace.domain.mypage.service.apply.ApplyDisplayStatus;
import com.kt.onrace.domain.mypage.service.apply.ApplyDisplayStatusContext;
import com.kt.onrace.domain.mypage.service.apply.ApplyDisplayStatusResolver;
import com.kt.onrace.domain.mypage.service.apply.ApplyDisplaySurface;
import com.kt.onrace.domain.mypage.service.apply.ApplyResultStatus;
import com.kt.onrace.domain.mypage.service.apply.ApplyUserStatus;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 사용자의 신청내역 목록과 기존 분리형 요약 목록을 함께 조회하는 서비스이다.
 * 기본 목록은 프론트 EventHistory 형식의 flat 리스트를 반환하고, overview용 요약 목록은 기존 계약을 유지한다.
 */
@ServiceLog
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationHistoryService {

	private static final List<OrderStatus> ORDER_OWNED_APPLICATION_STATUSES = List.of(
		OrderStatus.PENDING,
		OrderStatus.PAID,
		OrderStatus.CANCELLED
	);

	private final JPAQueryFactory queryFactory;
	private final ApplyDisplayStatusResolver applyDisplayStatusResolver;

	public List<MyPageApplicationHistoryItemDto> getEntries(
		Long userId,
		MyPageApplicationHistoryFilter filter
	) {
		List<Entry> entries = queryFactory.selectFrom(entry)
			.join(entry.event, event).fetchJoin()
			.join(entry.eventCourse, eventCourse).fetchJoin()
			.join(entry.eventPace, eventPace).fetchJoin()
			.where(applicationHistoryCondition(userId), appTypeCondition(filter))
			.orderBy(entry.createdAt.desc())
			.fetch();

		return entries.stream()
			.map(this::toApplicationHistoryItem)
			.toList();
	}

	public MyPageEntryListResponseDto getSummaryEntries(Long userId, int page, int size) {
		MyPagePagingPolicy.validate(page, size);

		long totalCount = countSummaryEntries(userId, false);
		if (totalCount == 0) {
			return MyPageEntryListResponseDto.empty(page, size);
		}

		List<MyPageEntryItemDto> items = queryFactory.selectFrom(entry)
			.join(entry.event, event).fetchJoin()
			.join(entry.eventCourse, eventCourse).fetchJoin()
			.join(entry.eventPace, eventPace).fetchJoin()
			.where(summaryEntriesCondition(userId))
			.orderBy(entry.createdAt.desc())
			.offset(MyPagePagingPolicy.offset(page, size))
			.limit(size)
			.fetch()
			.stream()
			.map(this::toSummaryEntryItem)
			.toList();

		return MyPageEntryListResponseDto.of(page, size, totalCount, items);
	}

	public MyPageEntryListResponseDto getWaitingEntries(Long userId, int page, int size) {
		MyPagePagingPolicy.validate(page, size);

		long totalCount = countSummaryEntries(userId, true);
		if (totalCount == 0) {
			return MyPageEntryListResponseDto.empty(page, size);
		}

		List<MyPageEntryItemDto> items = queryFactory.selectFrom(entry)
			.join(entry.event, event).fetchJoin()
			.join(entry.eventCourse, eventCourse).fetchJoin()
			.join(entry.eventPace, eventPace).fetchJoin()
			.where(waitingEntriesCondition(userId))
			.orderBy(entry.createdAt.desc())
			.offset(MyPagePagingPolicy.offset(page, size))
			.limit(size)
			.fetch()
			.stream()
			.map(this::toSummaryEntryItem)
			.toList();

		return MyPageEntryListResponseDto.of(page, size, totalCount, items);
	}

	private long countSummaryEntries(Long userId, boolean waitingOnly) {
		Long count = queryFactory.select(entry.count())
			.from(entry)
			.where(waitingOnly ? waitingEntriesCondition(userId) : summaryEntriesCondition(userId))
			.fetchOne();

		return count == null ? 0 : count;
	}

	private BooleanExpression summaryEntriesCondition(Long userId) {
		return entry.userId.eq(userId)
			.and(entry.status.in(EntryStatus.APPLIED, EntryStatus.WON, EntryStatus.LOST))
			.and(noOrderOwnedApplicationCondition(userId));
	}

	private BooleanExpression applicationHistoryCondition(Long userId) {
		return entry.userId.eq(userId)
			.and(
				entry.status.in(EntryStatus.APPLIED, EntryStatus.WON, EntryStatus.LOST)
					.or(
						event.appType.eq(EventAppType.FIRST_COME)
							.and(entry.status.in(EntryStatus.PRE_SAVED, EntryStatus.RESERVED))
					)
			)
			.and(noOrderOwnedApplicationCondition(userId));
	}

	private BooleanExpression appTypeCondition(MyPageApplicationHistoryFilter filter) {
		if (filter == null || filter.appType() == null) {
			return null;
		}

		return event.appType.eq(filter.appType());
	}

	private BooleanExpression waitingEntriesCondition(Long userId) {
		return entry.userId.eq(userId)
			.and(entry.status.in(EntryStatus.PRE_SAVED, EntryStatus.RESERVED))
			.and(noOrderOwnedApplicationCondition(userId));
	}

	private BooleanExpression noOrderOwnedApplicationCondition(Long userId) {
		com.kt.onrace.domain.order.entity.QOrder ownedOrder = new com.kt.onrace.domain.order.entity.QOrder("ownedOrder");
		com.kt.onrace.domain.event.entity.QEventCourse ownedOrderCourse =
			new com.kt.onrace.domain.event.entity.QEventCourse("ownedOrderCourse");

		// entry_id backfill 전까지는 legacy row를 user_id + event_id로 fallback 조회한다.
		return JPAExpressions.selectOne()
			.from(ownedOrder)
			.leftJoin(ownedOrderCourse).on(ownedOrderCourse.id.eq(ownedOrder.eventCourseId))
			.where(
				ownedOrder.userId.eq(userId),
				ownedOrder.orderStatus.in(ORDER_OWNED_APPLICATION_STATUSES),
				ownedOrder.entryId.eq(entry.id)
					.or(
						ownedOrder.entryId.isNull()
							.and(ownedOrderCourse.event.id.eq(entry.event.id))
					)
			)
			.notExists();
	}

	private MyPageApplicationHistoryItemDto toApplicationHistoryItem(Entry currentEntry) {
		ApplyDisplayDecision decision = resolveApplyDisplayDecision(ApplyDisplaySurface.APPLICATION_HISTORY, currentEntry);
		var currentEvent = currentEntry.getEvent();
		EventCourse course = requireEventCourse(currentEntry);
		EventPace pace = requireEventPace(currentEntry);

		return MyPageApplicationHistoryItemDto.of(
			currentEntry.getId(),
			currentEvent.getId(),
			currentEvent.getTitle(),
			currentEvent.getAppType(),
			currentEvent.getStatus(),
			resolveEntryStatus(decision.displayStatus()),
			currentEntry.getCreatedAt(),
			currentEvent.getEventAt(),
			currentEvent.getAppStartAt(),
			currentEvent.getAppEndAt(),
			resolveResultAt(currentEvent),
			currentEvent.getVenue(),
			course.getName(),
			pace.getName()
		);
	}

	private MyPageEntryItemDto toSummaryEntryItem(Entry currentEntry) {
		MyPageStatusDto status = toMyPageStatusDto(resolveApplyDisplayDecision(ApplyDisplaySurface.SUMMARY, currentEntry));
		EventCourse course = requireEventCourse(currentEntry);
		EventPace pace = requireEventPace(currentEntry);

		return MyPageEntryItemDto.of(
			currentEntry.getId(),
			currentEntry.getEvent().getId(),
			status.statusText(),
			null,
			currentEntry.getEvent().getTitle(),
			course.getName(),
			pace.getName(),
			course.getPrice(),
			currentEntry.getCreatedAt(),
			resolveResultAt(currentEntry.getEvent())
		);
	}

	private LocalDateTime resolveResultAt(Event currentEvent) {
		if (currentEvent.getAppType() != EventAppType.LOTTERY) {
			return null;
		}

		return currentEvent.getLotteryAnnouncedAt();
	}

	private EventCourse requireEventCourse(Entry currentEntry) {
		EventCourse course = currentEntry.getEventCourse();
		if (course == null) {
			throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
		}
		return course;
	}

	private EventPace requireEventPace(Entry currentEntry) {
		EventPace pace = currentEntry.getEventPace();
		if (pace == null) {
			throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
		}
		return pace;
	}

	private ApplyDisplayDecision resolveApplyDisplayDecision(ApplyDisplaySurface surface, Entry currentEntry) {
		return applyDisplayStatusResolver.resolve(new ApplyDisplayStatusContext(
			surface,
			currentEntry.getEvent().getAppType(),
			currentEntry.getEvent().getStatus(),
			resolveApplyUserStatus(currentEntry.getStatus()),
			resolveApplyResultStatus(currentEntry.getStatus())
		));
	}

	private ApplyUserStatus resolveApplyUserStatus(EntryStatus entryStatus) {
		return switch (entryStatus) {
			case PRE_SAVED -> ApplyUserStatus.PRE_SAVED;
			case RESERVED -> ApplyUserStatus.RESERVED;
			case APPLIED, WON, LOST -> ApplyUserStatus.APPLIED;
		};
	}

	private ApplyResultStatus resolveApplyResultStatus(EntryStatus entryStatus) {
		return switch (entryStatus) {
			case WON -> ApplyResultStatus.WON;
			case LOST -> ApplyResultStatus.LOST;
			case PRE_SAVED, RESERVED, APPLIED -> ApplyResultStatus.NONE;
		};
	}

	private MyPageStatusDto toMyPageStatusDto(ApplyDisplayDecision decision) {
		return MyPageStatusDto.of(
			decision.displayStatus().label(),
			decision.actionType().code(),
			decision.actionType().label(),
			decision.actionEnabled()
		);
	}

	private String resolveEntryStatus(ApplyDisplayStatus displayStatus) {
		return switch (displayStatus) {
			case PRE_ENTRY_SAVED, WAITING_TO_APPLY -> "신청 대기";
			case AVAILABLE_TO_APPLY -> "신청 가능";
			case ENTRY_CLOSED, ENTRY_UNAVAILABLE -> "신청 불가";
			case RESERVED, ENTRY_APPLIED -> "신청 완료";
			case LOTTERY_APPLIED -> "응모 완료";
			case RESULT_PENDING, RESULT_CHECK_REQUIRED -> "발표 대기";
			case WON -> "당첨";
			case LOST -> "미당첨";
		};
	}
}
