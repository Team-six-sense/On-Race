package com.kt.onrace.domain.entry.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.common.util.Preconditions;
import com.kt.onrace.domain.entry.dto.EntryStockCheckResponse;
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
import com.kt.onrace.domain.event.entity.EventAppType;
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
	private final EntryMetrics entryMetrics;

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
						.build());

		entryRepository.save(entry);

		log.info("[ENTRY] 사전정보 저장 userId={}, eventId={}, courseId={}, paceId={}", userId, eventId, request.courseId(), request.paceId());

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
					EntryCountResult counts = entryRepository
							.countTotalAndAppliedByPaceId(entry.getEventPace().getId());

					return EntryOverviewResponse.builder()
							.hasEntry(true)
							.entry(EntryOverviewResponse.EntryDto.from(entry))
							.courses(courses)
							.rateInfo(EntryOverviewResponse.RateInfoDto.of(
									counts.totalCount(), counts.appliedCount(),
									entry.getEventPace().getCapacity(), entry.getEventCourse().getPrice()))
							.build();
				})
				.orElseGet(() -> EntryOverviewResponse.builder()
						.hasEntry(false)
						.entry(null)
						.courses(courses)
						.rateInfo(null)
						.build());
	}

	@ServiceLog(slowMs = 2000)
	public EntryRateResponse getEntryRate(Long eventId, Long courseId, Long paceId) {
		EventPace pace = eventPaceRepository.findWithCourseOrThrow(
				paceId, courseId, eventId, BusinessErrorCode.ENTRY_PACE_NOT_FOUND);

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

		log.info("[ENTRY] 사전정보 삭제 userId={}, eventId={}, entryId={}", userId, eventId, entry.getId());

		return entry.getId();
	}

	@ServiceLog(slowMs = 2000)
	@Transactional
	public EntryApplyResponse apply(Long userId, Long eventId, EntryCoursePaceRequest request,
			Long queuePaceId, EventAppType expectedAppType) {
		if (queuePaceId != null) {
			Preconditions.validate(queuePaceId.equals(request.paceId()), BusinessErrorCode.ENTRY_QUEUE_PACE_MISMATCH);
		}

		memberRepository.findByIdAndIsDeletedFalseOrThrow(userId, BusinessErrorCode.MEMBER_NOT_FOUND);

		Event event = eventRepository.findByIdAndIsViewTrueAndIsDeletedFalseOrThrow(eventId,
				BusinessErrorCode.EVENT_NOT_FOUND);

		Preconditions.validate(event.getAppType() == expectedAppType, BusinessErrorCode.ENTRY_APP_TYPE_MISMATCH);

		LocalDateTime now = LocalDateTime.now();
		Preconditions.validate(event.getEventAt().isAfter(now), BusinessErrorCode.ENTRY_EVENT_ALREADY_ENDED);
		Preconditions.validate(!now.isBefore(event.getAppStartAt()) && !now.isAfter(event.getAppEndAt()),
				BusinessErrorCode.ENTRY_NOT_IN_PERIOD);

		EventCourse course = eventCourseRepository.findByIdAndEventIdOrThrow(request.courseId(), eventId,
				BusinessErrorCode.ENTRY_COURSE_NOT_FOUND);

		EventPace pace = eventPaceRepository.findByIdAndEventCourseIdOrThrow(request.paceId(), request.courseId(),
				BusinessErrorCode.ENTRY_PACE_NOT_FOUND);

		log.info("[ENTRY] 신청 시작 userId={}, eventId={}, appType={}, courseId={}, paceId={}",
			userId, eventId, expectedAppType, request.courseId(), request.paceId());

		return switch (event.getAppType()) {
			case LOTTERY -> applyLottery(userId, event, course, pace);
			case FIRST_COME -> applyFirstCome(userId, event, course, pace);
		};
	}

	private EntryApplyResponse applyLottery(Long userId, Event event, EventCourse course, EventPace pace) {
		Entry entry = getCreateEntry(userId, event);
		entry.apply(course, pace);
		entryRepository.save(entry);
		entryMetrics.recordApply("LOTTERY", "success");

		log.info("[ENTRY] 추첨 신청 완료 userId={}, eventId={}, entryId={}", userId, event.getId(), entry.getId());

		return EntryApplyResponse.from(entry);
	}

	private EntryApplyResponse applyFirstCome(Long userId, Event event, EventCourse course, EventPace pace) {
		Entry entry = getCreateEntry(userId, event);

		if (entry.isReserved()) {
			long remainingMs = eventStockService.getReservationTtl(entry.getEventPace().getId(), userId);
			if (remainingMs > 0) {
				return EntryApplyResponse.fromReserved(entry, LocalDateTime.now().plus(Duration.ofMillis(remainingMs)));
			}
		}

		long result = eventStockService.tryReserveStock(pace.getId(), userId);

		if (result == -2) {
			entryMetrics.recordApply("FIRST_COME", "duplicate");
			log.info("[ENTRY] 중복 선점 시도 userId={}, paceId={}", userId, pace.getId());
		} else if (result == -1) {
			entryMetrics.recordApply("FIRST_COME", "sold_out");
			log.info("[ENTRY] 선착순 매진 userId={}, paceId={}", userId, pace.getId());
		}

		Preconditions.validate(result != -2, BusinessErrorCode.ENTRY_ALREADY_RESERVED);
		Preconditions.validate(result != -1, BusinessErrorCode.ENTRY_SOLD_OUT);

		entryMetrics.recordApply("FIRST_COME", "success");
		log.info("[ENTRY] 선착순 선점 성공 userId={}, paceId={}, remaining={}", userId, pace.getId(), result);

		entry.reserve(course, pace);
		entryRepository.save(entry);

		return EntryApplyResponse.fromReserved(entry, LocalDateTime.now().plusSeconds(entryProperties.getTtlSeconds()));
	}

	private Entry getCreateEntry(Long userId, Event event) {
		return entryRepository.findByUserIdAndEventId(userId, event.getId())
				.map(e -> {
					Preconditions.validate(e.getStatus() != EntryStatus.APPLIED, BusinessErrorCode.ENTRY_ALREADY_APPLIED);
					Preconditions.validate(e.getStatus() == EntryStatus.RESERVED || e.getStatus() == EntryStatus.PRE_SAVED,
						BusinessErrorCode.ENTRY_CANNOT_APPLY);
					return e;
				})
				.orElseGet(() -> Entry.builder()
						.userId(userId)
						.event(event)
						.build());
	}

	@ServiceLog
	public EntryStockCheckResponse checkStock(Long paceId) {
		long available = eventStockService.getTempStock(paceId);

		if(available > 0) {
			EntryStockCheckResponse result = EntryStockCheckResponse.available(available);
			log.debug("[STOCK] 재고 조회 paceId={}, status={}, available={}", paceId, result.stockStatus(), result.remainingStock());
			return result;
		}

		long total = eventStockService.getTotalStock(paceId);
		long confirmed = eventStockService.getConfirmStock(paceId);

		if(confirmed >= total) {
			log.debug("[STOCK] 재고 조회 paceId={}, status=SOLD_OUT, available=0", paceId);
			return EntryStockCheckResponse.soldOut();
		}

		log.debug("[STOCK] 재고 조회 paceId={}, status=TEMP_SOLD_OUT, available=0", paceId);
		return EntryStockCheckResponse.tempSoldOut();
	}

	@ServiceLog(slowMs = 2000)
	@Transactional
	public void confirmReservation(Long userId, Long paceId, EventAppType type) {
		Entry entry = entryRepository.findByUserIdAndEventPaceId(userId, paceId)
				.orElseThrow(() -> new BusinessException(BusinessErrorCode.ENTRY_NOT_FOUND));


		if(type == EventAppType.FIRST_COME) {
			Preconditions.validate(entry.isReserved(), BusinessErrorCode.ENTRY_CANNOT_APPLY);
			Preconditions.validate(eventStockService.hasReservation(paceId, userId),
				BusinessErrorCode.ENTRY_RESERVATION_EXPIRED);

			entry.confirmPayment();
		}

		eventStockRepository.incrementConfirmedStock(paceId);

		entryMetrics.recordConfirm(type.name());
		log.info("[ENTRY] 결제 확정 userId={}, paceId={}, appType={}", userId, paceId, type);

		// 왜? 위의 if로 넣지 않느냐 -> 트랜잭션 커밋이 안된 상태에서 redis는 즉시 실행되고 DB 업데이트는 트랜잭션 커밋 시점에 반영되기
		// 때문에 DB가 실패할 시 redis는 롤백이 안되므로, DB가 성공적으로 끝난 후 redis를 실행하는 것이 맞음
		if(type == EventAppType.FIRST_COME) {
			eventStockService.deleteReservation(paceId, userId);
			eventStockService.confirmStock(paceId);
		}
	}

}
