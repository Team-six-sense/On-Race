package com.kt.onrace.domain.mypage.service;

import static com.kt.onrace.domain.entry.entity.QEntry.entry;
import static com.kt.onrace.domain.event.entity.QEvent.event;
import static com.kt.onrace.domain.event.entity.QEventCourse.eventCourse;
import static com.kt.onrace.domain.event.entity.QEventImage.eventImage;
import static com.kt.onrace.domain.event.entity.QEventPace.eventPace;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventImageType;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryFilter;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;
import com.kt.onrace.domain.mypage.service.apply.ApplyDisplayDecision;
import com.kt.onrace.domain.mypage.service.apply.ApplyDisplayStatusContext;
import com.kt.onrace.domain.mypage.service.apply.ApplyDisplayStatusResolver;
import com.kt.onrace.domain.mypage.service.apply.ApplyDisplaySurface;
import com.kt.onrace.domain.mypage.service.apply.ApplyResultStatus;
import com.kt.onrace.domain.mypage.service.apply.ApplyUserStatus;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 사용자의 신청내역 목록과 기존 분리형 요약 목록을 함께 조회하는 서비스이다.
 * 기본 목록은 혼합형 필터/빈 상태를 제공하고, overview용 요약 목록은 기존 계약을 유지한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationHistoryService {

	private final JPAQueryFactory queryFactory;
	private final ApplyDisplayStatusResolver applyDisplayStatusResolver;

	public MyPageApplicationHistoryListResponseDto getEntries(
		Long userId,
		MyPageApplicationHistoryFilter filter,
		int page,
		int size
	) {
		MyPagePagingPolicy.validate(page, size);

		ApplicationHistoryCounts counts = countApplicationHistoryEntries(userId);
		long totalCount = filter.pickCount(counts.allCount(), counts.lotteryCount(), counts.firstComeCount());
		if (totalCount == 0) {
			return MyPageApplicationHistoryListResponseDto.empty(
				filter.name(),
				counts.toDto(),
				page,
				size,
				filter.emptyTitle(),
				filter.emptyDescription()
			);
		}

		List<Entry> entries = queryFactory.selectFrom(entry)
			.join(entry.event, event).fetchJoin()
			.join(entry.eventCourse, eventCourse).fetchJoin()
			.join(entry.eventPace, eventPace).fetchJoin()
			.where(applicationHistoryCondition(userId), appTypeCondition(filter))
			.orderBy(entry.createdAt.desc())
			.offset(MyPagePagingPolicy.offset(page, size))
			.limit(size)
			.fetch();

		Map<Long, String> thumbnailByEventId = loadThumbnailByEventId(entries);
		List<MyPageApplicationHistoryItemDto> items = entries.stream()
			.map(currentEntry -> toApplicationHistoryItem(
				currentEntry,
				thumbnailByEventId.get(currentEntry.getEvent().getId())
			))
			.toList();

		return MyPageApplicationHistoryListResponseDto.of(
			filter.name(),
			counts.toDto(),
			page,
			size,
			totalCount,
			items
		);
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
			.and(noPaidOrderCondition(userId));
	}

	private BooleanExpression applicationHistoryCondition(Long userId) {
		return entry.userId.eq(userId)
			.and(
				entry.status.in(EntryStatus.APPLIED, EntryStatus.WON, EntryStatus.LOST)
					.or(
						event.appType.eq(EventAppType.FIRST_COME)
							.and(entry.status.in(EntryStatus.PRE_SAVED, EntryStatus.RESERVED))
					)
			);
	}

	private BooleanExpression appTypeCondition(MyPageApplicationHistoryFilter filter) {
		if (filter == null || filter.appType() == null) {
			return null;
		}

		return event.appType.eq(filter.appType());
	}

	private BooleanExpression waitingEntriesCondition(Long userId) {
		return entry.userId.eq(userId)
			.and(entry.status.in(EntryStatus.PRE_SAVED, EntryStatus.RESERVED));
	}

	private BooleanExpression noPaidOrderCondition(Long userId) {
		com.kt.onrace.domain.order.entity.QOrder paidOrder = new com.kt.onrace.domain.order.entity.QOrder("paidOrder");

		return JPAExpressions.selectOne()
			.from(paidOrder)
			.where(
				paidOrder.userId.eq(userId),
				paidOrder.eventCourseId.eq(entry.eventCourse.id),
				paidOrder.eventPaceId.eq(entry.eventPace.id),
				paidOrder.orderStatus.eq(OrderStatus.PAID)
			)
			.notExists();
	}

	private ApplicationHistoryCounts countApplicationHistoryEntries(Long userId) {
		com.querydsl.core.types.Expression<Long> countExpression = entry.count();

		List<Tuple> rows = queryFactory.select(event.appType, countExpression)
			.from(entry)
			.join(entry.event, event)
			.where(applicationHistoryCondition(userId))
			.groupBy(event.appType)
			.fetch();

		long lotteryCount = 0;
		long firstComeCount = 0;
		for (Tuple row : rows) {
			EventAppType appType = row.get(event.appType);
			Long count = row.get(countExpression);
			if (appType == EventAppType.LOTTERY) {
				lotteryCount = count == null ? 0 : count;
			}
			if (appType == EventAppType.FIRST_COME) {
				firstComeCount = count == null ? 0 : count;
			}
		}

		return new ApplicationHistoryCounts(
			lotteryCount + firstComeCount,
			lotteryCount,
			firstComeCount
		);
	}

	private Map<Long, String> loadThumbnailByEventId(List<Entry> entries) {
		Set<Long> eventIds = entries.stream()
			.map(Entry::getEvent)
			.map(com.kt.onrace.domain.event.entity.Event::getId)
			.collect(Collectors.toSet());
		if (eventIds.isEmpty()) {
			return Map.of();
		}

		List<Tuple> rows = queryFactory.select(eventImage.event.id, eventImage.url)
			.from(eventImage)
			.where(
				eventImage.event.id.in(eventIds),
				eventImage.type.eq(EventImageType.THUMBNAIL)
			)
			.orderBy(eventImage.event.id.asc(), eventImage.sort.asc())
			.fetch();

		Map<Long, String> thumbnailByEventId = new java.util.LinkedHashMap<>();
		for (Tuple row : rows) {
			Long eventId = row.get(eventImage.event.id);
			String thumbnailUrl = row.get(eventImage.url);
			thumbnailByEventId.putIfAbsent(eventId, thumbnailUrl);
		}
		return thumbnailByEventId;
	}

	private MyPageApplicationHistoryItemDto toApplicationHistoryItem(Entry currentEntry, String thumbnailUrl) {
		MyPageStatusDto status = toMyPageStatusDto(resolveApplyDisplayDecision(ApplyDisplaySurface.APPLICATION_HISTORY, currentEntry));
		String courseName = currentEntry.getEventCourse() != null ? currentEntry.getEventCourse().getName() : null;
		String paceName = currentEntry.getEventPace() != null ? currentEntry.getEventPace().getName() : null;

		return new MyPageApplicationHistoryItemDto(
			currentEntry.getId(),
			currentEntry.getEvent().getId(),
			currentEntry.getCreatedAt(),
			thumbnailUrl,
			currentEntry.getEvent().getTitle(),
			new MyPageApplicationHistoryItemDto.SelectedOption(
				courseName,
				paceName,
				buildSelectedOptionDisplay(courseName, paceName)
			),
			new MyPageApplicationHistoryItemDto.EventMethod(
				currentEntry.getEvent().getAppType().name(),
				resolveEventMethodLabel(currentEntry.getEvent().getAppType())
			),
			new MyPageApplicationHistoryItemDto.RecruitmentSchedule(
				currentEntry.getEvent().getAppStartAt(),
				currentEntry.getEvent().getAppEndAt()
			),
			status.statusText(),
			new MyPageApplicationHistoryItemDto.Action(
				status.actionType(),
				status.actionLabel(),
				status.actionEnabled()
			)
		);
	}

	private MyPageEntryItemDto toSummaryEntryItem(Entry currentEntry) {
		MyPageStatusDto status = toMyPageStatusDto(resolveApplyDisplayDecision(ApplyDisplaySurface.SUMMARY, currentEntry));

		return new MyPageEntryItemDto(
			currentEntry.getId(),
			currentEntry.getEvent().getId(),
			status.statusText(),
			status.actionType(),
			status.actionLabel(),
			status.actionEnabled(),
			null,
			currentEntry.getEvent().getTitle(),
			currentEntry.getEventCourse() != null ? currentEntry.getEventCourse().getName() : null,
			currentEntry.getEventPace() != null ? currentEntry.getEventPace().getName() : null,
			currentEntry.getEventCourse() != null ? currentEntry.getEventCourse().getPrice() : null,
			currentEntry.getCreatedAt(),
			currentEntry.getEvent().getLotteryAnnouncedAt()
		);
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

	private String buildSelectedOptionDisplay(String courseName, String paceName) {
		if (courseName == null && paceName == null) {
			return null;
		}

		if (courseName == null) {
			return paceName;
		}

		if (paceName == null) {
			return courseName;
		}

		return courseName + " / " + paceName;
	}

	private String resolveEventMethodLabel(EventAppType appType) {
		return switch (appType) {
			case LOTTERY -> "추첨";
			case FIRST_COME -> "선착";
		};
	}

	private record ApplicationHistoryCounts(
		long allCount,
		long lotteryCount,
		long firstComeCount
	) {
		private MyPageApplicationHistoryListResponseDto.Counts toDto() {
			return new MyPageApplicationHistoryListResponseDto.Counts(allCount, lotteryCount, firstComeCount);
		}
	}
}
