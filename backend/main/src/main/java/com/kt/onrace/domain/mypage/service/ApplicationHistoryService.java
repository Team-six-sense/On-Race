package com.kt.onrace.domain.mypage.service;

import static com.kt.onrace.domain.entry.entity.QEntry.entry;
import static com.kt.onrace.domain.event.entity.QEvent.event;
import static com.kt.onrace.domain.event.entity.QEventCourse.eventCourse;
import static com.kt.onrace.domain.event.entity.QEventPace.eventPace;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.mypage.dto.MyPageEntryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageEntryListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 사용자의 신청 이력과 신청 대기 이력을 마이페이지용 목록으로 조회하는 서비스이다.
 * 화면 규칙에 맞춰 상태를 해석하고 결제 완료 주문이 있는 신청은 제외한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationHistoryService {

	private final JPAQueryFactory queryFactory;
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

	private long countEntries(Long userId, boolean waitingOnly) {
		Long count = queryFactory.select(entry.count())
			.from(entry)
			.where(waitingOnly ? waitingEntriesCondition(userId) : baseEntriesCondition(userId))
			.fetchOne();

		return count == null ? 0 : count;
	}

	private BooleanExpression baseEntriesCondition(Long userId) {
		return entry.userId.eq(userId)
			.and(entry.status.in(EntryStatus.APPLIED, EntryStatus.WON, EntryStatus.LOST))
			.and(noPaidOrderCondition(userId));
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
}
