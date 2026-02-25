package com.kt.onrace.domain.entry.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.common.util.Preconditions;
import com.kt.onrace.domain.entry.dto.EntryPreSaveRequest;
import com.kt.onrace.domain.entry.dto.EntryPreSaveResponse;
import com.kt.onrace.domain.entry.dto.EntryInfoResponse;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntryService {

	private final EventRepository eventRepository;
	private final EventCourseRepository eventCourseRepository;
	private final EntryRepository entryRepository;
	private final EventPaceRepository eventPaceRepository;

	@ServiceLog(slowMs = 2000)
	@Transactional
	public EntryPreSaveResponse savePreSave(Long userId, Long eventId, EntryPreSaveRequest request) {
		//TODO : User 검증 추가

		Event event = eventRepository.findByIdOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);

		Preconditions.validate(event.getStatus() == EventStatus.READY, BusinessErrorCode.EVENT_NOT_IN_STANDBY);

		EventCourse course = eventCourseRepository.findByIdAndEventIdOrThrow(request.courseId(), eventId, BusinessErrorCode.ENTRY_COURSE_NOT_FOUND);

		EventPace pace = eventPaceRepository.findByIdAndEventCourseIdOrThrow(request.paceId(), request.courseId(), BusinessErrorCode.ENTRY_PACE_NOT_FOUND);

		Entry entry = entryRepository.findByUserIdAndEventId(userId, eventId)
			.map(e -> {
				Preconditions.validate(e.getStatus() == EntryStatus.PRE_SAVED, BusinessErrorCode.ENTRY_EVENT_NOT_IN_STANDBY);
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
	public EntryInfoResponse getParticipationInfo(Long eventId, Long courseId, Long paceId) {
		eventRepository.findByIdOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);

		EventCourse course = eventCourseRepository.findByIdAndEventIdOrThrow(courseId, eventId, BusinessErrorCode.EVENT_COURSE_NOT_FOUND);

		EventPace pace = eventPaceRepository.findByIdAndEventCourseIdOrThrow(paceId, courseId, BusinessErrorCode.EVENT_PACE_NOT_FOUND);

		long entryCount = entryRepository.countByEventPaceIdAndStatusIn(
			paceId, List.of(EntryStatus.PRE_SAVED, EntryStatus.APPLIED)
		);

		// TODO 계산 로직은 추후 분리 및 수정할 예정입니다(프론트 협업위해 가능 서비스 용도)
		int capacity = pace.getCapacity();

		double competitionRate = 0;
		double fillRatePercent = 0;

		if (capacity > 0) {
			if (entryCount > capacity) {
				competitionRate = Math.round((double) entryCount / capacity * 10) / 10.0;
			} else {
				fillRatePercent = Math.round((double) entryCount / capacity * 1000) / 10.0;
			}
		}

		return EntryInfoResponse.builder()
			.entryCount(entryCount)
			.capacity(capacity)
			.competitionRate(competitionRate)
			.fillRatePercent(fillRatePercent)
			.price(course.getPrice())
			.build();
	}

	@ServiceLog(slowMs = 2000)
	@Transactional
	public Long deletePreSave(Long userId, Long eventId) {
		//TODO : User 검증 추가

		Entry entry = entryRepository.findByUserIdAndEventIdOrThrow(userId, eventId, BusinessErrorCode.ENTRY_NOT_FOUND);

		Preconditions.validate(entry.getStatus() == EntryStatus.PRE_SAVED, BusinessErrorCode.ENTRY_EVENT_NOT_IN_STANDBY);

		entryRepository.deleteByUserIdAndEventId(userId, eventId);

		return entry.getId();
	}
}
