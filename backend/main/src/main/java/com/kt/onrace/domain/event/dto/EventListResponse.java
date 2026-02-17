package com.kt.onrace.domain.event.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.kt.onrace.domain.event.entity.EventType;

import lombok.Builder;

@Builder
public record EventListResponse(
	Long id,
	String title,
	EventStatus status,
	EventType type,
	String thumbnailImg,
	LocalDateTime eventAt,
	LocalDateTime appStartAt,
	LocalDateTime appEndAt,
	String venueAddress,
	List<CourseDto> courses
) {

	@Builder
	public record CourseDto(
		Long id,
		String name,
		int distanceM,
		long price
	) {
	}

	public static EventListResponse from(Event event) {
		List<CourseDto> courses = event.getCourses().stream()
			.map(course -> CourseDto.builder()
				.id(course.getId())
				.name(course.getName())
				.distanceM(course.getDistanceM())
				.price(course.getPrice())
				.build())
			.toList();

		return EventListResponse.builder()
			.id(event.getId())
			.title(event.getTitle())
			.status(event.getStatus())
			.type(event.getType())
			.thumbnailImg(event.getThumbnailImg())
			.eventAt(event.getEventAt())
			.appStartAt(event.getAppStartAt())
			.appEndAt(event.getAppEndAt())
			.venueAddress(event.getVenueAddress())
			.courses(courses)
			.build();
	}
}
