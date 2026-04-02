package com.kt.onrace.domain.entry.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.common.util.Preconditions;
import com.kt.onrace.domain.entry.config.EntryProperties;
import com.kt.onrace.domain.entry.dto.EntryApplyResponse;
import com.kt.onrace.domain.entry.dto.EntryCountResult;
import com.kt.onrace.domain.entry.dto.EntryCoursePaceRequest;
import com.kt.onrace.domain.entry.dto.EntryOverviewResponse;
import com.kt.onrace.domain.entry.dto.EntryPreSaveResponse;
import com.kt.onrace.domain.entry.dto.EntryRateResponse;
import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.entry.repository.EntryRepository;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.kt.onrace.domain.event.repository.EventCourseRepository;
import com.kt.onrace.domain.event.repository.EventPaceRepository;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.event.repository.EventStockRepository;
import com.kt.onrace.domain.event.service.EventStockService;
import com.kt.onrace.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntryService {

	private final EntryProperties entryProperties;
	private final EventRepository eventRepository;
	private final EventCourseRepository eventCourseRepository;
	private final EntryRepository entryRepository;
	private final EventPaceRepository eventPaceRepository;
	private final MemberRepository memberRepository;
	private final EventStockService eventStockService;
	private final EventStockRepository eventStockRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	@ServiceLog(slowMs = 2000)
	@Transactional
	public EntryPreSaveResponse savePreSave(Long userId, Long eventId, EntryCoursePaceRequest request) {
		memberRepository.findByIdAndIsDeletedFalseOrThrow(userId, BusinessErrorCode.MEMBER_NOT_FOUND);

		Event event = eventRepository.findByIdAndIsViewTrueAndIsDeletedFalseOrThrow(eventId,
			BusinessErrorCode.EVENT_NOT_FOUND);

		Preconditions.validate(event.getStatus() == EventStatus.READY, BusinessErrorCode.EVENT_NOT_IN_STANDBY);

		EventCourse course = eventCourseRepository.findByIdAndEventIdOrThrow(request.courseId(), eventId,
			BusinessErrorCode.ENTRY_COURSE_NOT_FOUND);

		EventPace pace = eventPaceRepository.findByIdAndEventCourseIdOrThrow(request.paceId(), request.courseId(),
			BusinessErrorCode.ENTRY_PACE_NOT_FOUND);

		Entry entry = entryRepository.findByUserIdAndEventId(userId, eventId)
			.map(e -> {
				Preconditions.validate(e.getStatus() == EntryStatus.PRE_SAVED,
					BusinessErrorCode.ENTRY_EVENT_NOT_IN_STANDBY);
				e.updatePreSave(course, pace);
				return e;
			})
			.orElseGet(() -> Entry.builder()
				.userId(userId)
				.event(event)
				.eventCourse(course)
				.eventPace(pace)
				.status(EntryStatus.PRE_SAVED)
				.build()
			);

		entryRepository.save(entry);

		return EntryPreSaveResponse.from(entry);
	}

	@ServiceLog(slowMs = 2000)
	public EntryOverviewResponse getEntryOverview(Long userId, Long eventId) {
		Event event = eventRepository.findEventWithCoursesAndPacesOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);

		List<EntryOverviewResponse.CourseOptionDto> courses = event.getCourses().stream()
			.map(EntryOverviewResponse.CourseOptionDto::from)
			.toList();

		// 비로그인 사용자일 경우(코스/페이스 목록만 반환)
		if (userId == null) {
			return EntryOverviewResponse.builder()
				.hasEntry(false)
				.entry(null)
				.courses(courses)
				.rateInfo(null)
				.build();
		}

		// 로그인 사용자(사전정보 조회 포함임)
		return entryRepository.findByUserIdAndEventId(userId, eventId)
			.map(entry -> {
				EntryCountResult counts = entryRepository.countTotalAndAppliedByPaceId(entry.getEventPace().getId());

				return EntryOverviewResponse.builder()
					.hasEntry(true)
					.entry(EntryOverviewResponse.EntryDto.from(entry))
					.courses(courses)
					.rateInfo(EntryOverviewResponse.RateInfoDto.of(
						counts.totalCount(), counts.appliedCount(),
						entry.getEventPace().getCapacity(), entry.getEventCourse().getPrice()
					))
					.build();
			})
			.orElseGet(() -> EntryOverviewResponse.builder()
				.hasEntry(false)
				.entry(null)
				.courses(courses)
				.rateInfo(null)
				.build()
			);
	}

	@ServiceLog(slowMs = 2000)
	public EntryRateResponse getEntryRate(Long eventId, Long courseId, Long paceId) {
		EventPace pace = eventPaceRepository.findWithCourseOrThrow(
			paceId, courseId, eventId, BusinessErrorCode.ENTRY_PACE_NOT_FOUND
		);

		EntryCountResult counts = entryRepository.countTotalAndAppliedByPaceId(paceId);

		return EntryRateResponse.of(counts.totalCount(), counts.appliedCount(), pace.getCapacity(),
			pace.getEventCourse().getPrice());
	}

	@ServiceLog(slowMs = 2000)
	@Transactional
	public Long deletePreSave(Long userId, Long eventId) {
		memberRepository.findByIdAndIsDeletedFalseOrThrow(userId, BusinessErrorCode.MEMBER_NOT_FOUND);

		Entry entry = entryRepository.findByUserIdAndEventIdOrThrow(userId, eventId, BusinessErrorCode.ENTRY_NOT_FOUND);

		Preconditions.validate(entry.getStatus() == EntryStatus.PRE_SAVED,
			BusinessErrorCode.ENTRY_EVENT_NOT_IN_STANDBY);

		entryRepository.deleteByUserIdAndEventId(userId, eventId);

		return entry.getId();
	}

	@ServiceLog(slowMs = 2000)
	@Transactional
	public EntryApplyResponse apply(Long userId, Long eventId, EntryCoursePaceRequest request) {
		memberRepository.findByIdAndIsDeletedFalseOrThrow(userId, BusinessErrorCode.MEMBER_NOT_FOUND);

		Event event = eventRepository.findByIdAndIsViewTrueAndIsDeletedFalseOrThrow(eventId,
			BusinessErrorCode.EVENT_NOT_FOUND);

		LocalDateTime now = LocalDateTime.now();
		Preconditions.validate(event.getEventAt().isAfter(now), BusinessErrorCode.ENTRY_EVENT_ALREADY_ENDED);
		Preconditions.validate(!now.isBefore(event.getAppStartAt()) && !now.isAfter(event.getAppEndAt()),
			BusinessErrorCode.ENTRY_NOT_IN_PERIOD);

		EventCourse course = eventCourseRepository.findByIdAndEventIdOrThrow(request.courseId(), eventId,
			BusinessErrorCode.ENTRY_COURSE_NOT_FOUND);

		EventPace pace = eventPaceRepository.findByIdAndEventCourseIdOrThrow(request.paceId(), request.courseId(),
			BusinessErrorCode.ENTRY_PACE_NOT_FOUND);

		return switch (event.getAppType()) {
			case LOTTERY -> applyLottery(userId, event, course, pace);
			case FIRST_COME -> applyFirstCome(userId, event, course, pace);
		};
	}

	private EntryApplyResponse applyLottery(Long userId, Event event, EventCourse course, EventPace pace) {
		Entry entry = getCreateEntry(userId, event);
		entry.apply(course, pace);
		entryRepository.save(entry);

		return EntryApplyResponse.from(entry);
	}

	private EntryApplyResponse applyFirstCome(Long userId, Event event, EventCourse course, EventPace pace) {
		Entry entry = getCreateEntry(userId, event);

		long result = eventStockService.tryReserveStock(pace.getId(), userId);
		Preconditions.validate(result != -2, BusinessErrorCode.ENTRY_ALREADY_RESERVED);
		Preconditions.validate(result != -1, BusinessErrorCode.ENTRY_SOLD_OUT);

		entry.reserve(course, pace);
		entryRepository.save(entry);

		return EntryApplyResponse.fromReserved(entry, LocalDateTime.now().plusSeconds(entryProperties.getTtlSeconds()));
	}

	private Entry getCreateEntry(Long userId, Event event) {
		return entryRepository.findByUserIdAndEventId(userId, event.getId())
			.map(e -> {
				switch (e.getStatus()) {
					case APPLIED -> throw new BusinessException(BusinessErrorCode.ENTRY_ALREADY_APPLIED);
					case RESERVED -> throw new BusinessException(BusinessErrorCode.ENTRY_ALREADY_RESERVED);
					case PRE_SAVED -> {
					} // 그대로 반환함
					default -> throw new BusinessException(BusinessErrorCode.ENTRY_CANNOT_APPLY);
				}
				return e;
			})
			.orElseGet(() -> Entry.builder()
				.userId(userId)
				.event(event)
				.build()
			);
	}

	/**
	 * 결제 확정 — RESERVED → APPLIED 전환, DB 확정 재고 증가
	 * Redis 예약 키 삭제는 트랜잭션 커밋 후 ReservationConfirmedListener에서 처리
	 */
	@ServiceLog(slowMs = 2000)
	@Transactional
	public void confirmReservation(Long userId, Long paceId) {
		Entry entry = entryRepository.findByUserIdAndEventPaceId(userId, paceId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ENTRY_NOT_FOUND));

		Preconditions.validate(entry.isReserved(), BusinessErrorCode.ENTRY_CANNOT_APPLY);
		Preconditions.validate(eventStockService.hasReservation(paceId, userId),
			BusinessErrorCode.ENTRY_RESERVATION_EXPIRED
		);

		entry.confirmPayment();
		eventStockRepository.findByEventPaceIdOrThrow(paceId).confirmStock();

		// applicationEventPublisher.publishEvent(new ReservationConfirmedEvent(paceId, userId));
	}

}
