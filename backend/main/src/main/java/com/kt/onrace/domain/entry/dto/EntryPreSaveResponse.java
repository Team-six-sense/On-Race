package com.kt.onrace.domain.entry.dto;

import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;

import lombok.Builder;

@Builder
public record EntryPreSaveResponse(
	Long id,
	Long eventId,
	EntryStatus status,
	CourseDto course,
	PaceDto pace
) {

	@Builder
	public record CourseDto(
		Long id,
		String name,
		int distanceM,
		long price
	) {

	}

	@Builder
	public record PaceDto(
		Long id,
		String name,
		int hour,
		int minutes
	) {

	}

	public static EntryPreSaveResponse from(Entry entry) {
		var course = entry.getEventCourse();
		var pace = entry.getEventPace();

		return EntryPreSaveResponse.builder()
			.id(entry.getId())
			.eventId(entry.getEvent().getId())
			.status(entry.getStatus())
			.course(CourseDto.builder()
				.id(course.getId())
				.name(course.getName())
				.distanceM(course.getDistanceM())
				.price(course.getPrice())
				.build())
			.pace(PaceDto.builder()
				.id(pace.getId())
				.name(pace.getName())
				.hour(pace.getHour())
				.minutes(pace.getMinutes())
				.build())
			.build();
	}
}
