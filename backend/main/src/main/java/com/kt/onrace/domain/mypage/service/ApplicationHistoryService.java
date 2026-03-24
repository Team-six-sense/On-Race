package com.kt.onrace.domain.mypage.service;

import static com.kt.onrace.domain.entry.entity.QEntry.entry;
import static com.kt.onrace.domain.event.entity.QEvent.event;
import static com.kt.onrace.domain.event.entity.QEventCourse.eventCourse;
import static com.kt.onrace.domain.event.entity.QEventPace.eventPace;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventImage;
import com.kt.onrace.domain.event.entity.EventImageType;
import com.kt.onrace.domain.event.repository.EventImageRepository;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryFilter;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageApplicationHistoryResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 사용자의 신청 이력과 신청 대기 이력을 마이페이지 응답으로 변환하는 서비스이다.
 * reduced scope 계약에서는 신청내역 화면 전용 응답과 레거시 분리 응답을 함께 유지한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationHistoryService {

	private final JPAQueryFactory queryFactory;
	private final EventImageRepository eventImageRepository;
	private final MyPageDisplayStatusResolver displayStatusResolver;

	public MyPageEntryListResponseDto getEntries(Long userId, int page, int size) {
		MyPagePagingPolicy.validate(page, size);

		long totalCount = countEntries(userId, false);
		if (totalCount == 0) {
			return MyPageEntryListResponseDto.empty(page, size);
		}

		List<MyPageEntryItemDto> items = queryFactory.selectFrom(entry)
			.join(entry.event, event).fetchJoin()
			.join(entry.eventCourse, eventCourse).fetchJoin()
			.join(entry.eventPace, eventPace).fetchJoin()
			.where(baseEntriesCondition(userId))
			.orderBy(entry.createdAt.desc())
			.offset(MyPagePagingPolicy.offset(page, size))
			.limit(size)
			.fetch()
			.stream()
			.map(this::toEntryItem)
			.toList();

		return MyPageEntryListResponseDto.of(page, size, totalCount, items);
	}

	public MyPageEntryListResponseDto getWaitingEntries(Long userId, int page, int size) {
		MyPagePagingPolicy.validate(page, size);

		long totalCount = countEntries(userId, true);
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
			.map(this::toEntryItem)
			.toList();

		return MyPageEntryListResponseDto.of(page, size, totalCount, items);
	}

	public MyPageApplicationHistoryResponseDto getApplicationHistory(
		Long userId,
		MyPageApplicationHistoryFilter filter,
		int page,
		int size
	) {
		MyPagePagingPolicy.validate(page, size);

		long totalCount = countApplicationHistory(userId, filter);
		if (totalCount == 0) {
			return MyPageApplicationHistoryResponseDto.empty(filter, page, size);
		}

		List<Entry> entries = queryFactory.selectFrom(entry)
			.join(entry.event, event).fetchJoin()
			.join(entry.eventCourse, eventCourse).fetchJoin()
			.join(entry.eventPace, eventPace).fetchJoin()
			.where(applicationHistoryCondition(userId, filter))
			.orderBy(entry.createdAt.desc())
			.offset(MyPagePagingPolicy.offset(page, size))
			.limit(size)
			.fetch();

		Map<Long, String> thumbnailByEventId = loadThumbnailByEventId(entries);
		List<MyPageApplicationHistoryItemDto> items = entries.stream()
			.map(currentEntry -> toApplicationHistoryItem(currentEntry, thumbnailByEventId))
			.toList();

		return MyPageApplicationHistoryResponseDto.of(filter, page, size, totalCount, items);
	}

	private long countEntries(Long userId, boolean waitingOnly) {
		Long count = queryFactory.select(entry.count())
			.from(entry)
			.where(waitingOnly ? waitingEntriesCondition(userId) : baseEntriesCondition(userId))
			.fetchOne();

		return count == null ? 0 : count;
	}

	private long countApplicationHistory(Long userId, MyPageApplicationHistoryFilter filter) {
		Long count = queryFactory.select(entry.count())
			.from(entry)
			.join(entry.event, event)
			.where(applicationHistoryCondition(userId, filter))
			.fetchOne();

		return count == null ? 0 : count;
	}

	private BooleanExpression baseEntriesCondition(Long userId) {
		return entry.userId.eq(userId)
			.and(entry.status.in(EntryStatus.APPLIED, EntryStatus.WON, EntryStatus.LOST));
	}

	private BooleanExpression waitingEntriesCondition(Long userId) {
		return entry.userId.eq(userId)
			.and(entry.status.in(EntryStatus.PRE_SAVED, EntryStatus.RESERVED));
	}

	private BooleanExpression applicationHistoryCondition(Long userId, MyPageApplicationHistoryFilter filter) {
		BooleanExpression condition = entry.userId.eq(userId)
			.and(entry.status.in(
				EntryStatus.PRE_SAVED,
				EntryStatus.RESERVED,
				EntryStatus.APPLIED,
				EntryStatus.WON,
				EntryStatus.LOST
			));

		return condition.and(applicationTypeCondition(filter));
	}

	private BooleanExpression applicationTypeCondition(MyPageApplicationHistoryFilter filter) {
		if (filter == null || filter == MyPageApplicationHistoryFilter.ALL) {
			return null;
		}

		return switch (filter) {
			case ALL -> null;
			case LOTTERY -> event.appType.eq(EventAppType.LOTTERY);
			case FIRST_COME -> event.appType.eq(EventAppType.FIRST_COME);
		};
	}

	private MyPageEntryItemDto toEntryItem(Entry currentEntry) {
		MyPageStatusDto status = displayStatusResolver.resolveApplicationStatus(currentEntry.getEvent(), currentEntry);

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

	private MyPageApplicationHistoryItemDto toApplicationHistoryItem(
		Entry currentEntry,
		Map<Long, String> thumbnailByEventId
	) {
		Event currentEvent = currentEntry.getEvent();
		MyPageStatusDto status = displayStatusResolver.resolveApplicationStatus(currentEvent, currentEntry);

		return new MyPageApplicationHistoryItemDto(
			currentEntry.getId(),
			currentEvent.getId(),
			currentEvent.getTitle(),
			currentEvent.getAppType(),
			currentEvent.getStatus(),
			status.statusText(),
			status.actionType(),
			status.actionLabel(),
			status.actionEnabled(),
			buildDeepLink(currentEvent.getId()),
			thumbnailByEventId.get(currentEvent.getId()),
			currentEntry.getEventCourse() != null ? currentEntry.getEventCourse().getName() : null,
			currentEntry.getEventPace() != null ? currentEntry.getEventPace().getName() : null,
			currentEntry.getEventCourse() != null ? currentEntry.getEventCourse().getPrice() : null,
			currentEntry.getCreatedAt(),
			currentEvent.getEventAt(),
			currentEvent.getAppStartAt(),
			currentEvent.getAppEndAt(),
			currentEvent.getLotteryAnnouncedAt()
		);
	}

	private Map<Long, String> loadThumbnailByEventId(Collection<Entry> entries) {
		Set<Long> eventIds = entries.stream()
			.map(Entry::getEvent)
			.map(Event::getId)
			.collect(Collectors.toSet());

		if (eventIds.isEmpty()) {
			return Map.of();
		}

		List<EventImage> thumbnails = eventImageRepository.findByEvent_IdInAndTypeOrderBySortAsc(
			eventIds,
			EventImageType.THUMBNAIL
		);

		Map<Long, String> thumbnailByEventId = new LinkedHashMap<>();
		for (EventImage thumbnail : thumbnails) {
			thumbnailByEventId.putIfAbsent(thumbnail.getEvent().getId(), thumbnail.getUrl());
		}
		return thumbnailByEventId;
	}

	private String buildDeepLink(Long eventId) {
		return "/ticketing/" + eventId;
	}
}
