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

		EventCourse course = eventCourseRepository.findByIdAndEventIdOrThrow(request.courseId(), eventId, BusinessErrorCode.EVENT_COURSE_NOT_FOUND);

		EventPace pace = eventPaceRepository.findByIdAndEventCourseIdOrThrow(request.paceId(), request.courseId(), BusinessErrorCode.EVENT_PACE_NOT_FOUND);

		Entry entry = entryRepository.findByUserIdAndEventId(userId, eventId)
			.map(e -> {
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

		int capacity = pace.getCapacity();
		boolean isCompetitive = capacity > 0 && entryCount > capacity;

		double rate;
		if (capacity == 0) {
			rate = 0;
		} else if (isCompetitive) {
			rate = Math.round((double) entryCount / capacity * 10) / 10.0;
		} else {
			rate = Math.round((double) entryCount / capacity * 1000) / 10.0;
		}

		return EntryInfoResponse.builder()
			.entryCount(entryCount)
			.capacity(capacity)
			.isCompetitive(isCompetitive)
			.rate(rate)
			.price(course.getPrice())
			.build();
	}

	@ServiceLog(slowMs = 2000)
	@Transactional
	public void deletePreSave(Long userId, Long eventId) {
		//TODO : User 검증 추가

		Entry entry = entryRepository.findByUserIdAndEventIdOrThrow(userId, eventId, BusinessErrorCode.ENTRY_NOT_FOUND);

		Preconditions.validate(entry.getStatus() == EntryStatus.PRE_SAVED, BusinessErrorCode.ENTRY_EVENT_NOT_IN_STANDBY);

		entryRepository.deleteByUserIdAndEventId(userId, eventId);
	}
}
