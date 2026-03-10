package com.kt.onrace.domain.event.dto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventImage;
import com.kt.onrace.domain.event.entity.EventImageType;
import com.kt.onrace.domain.event.entity.EventRegion;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventType;

import lombok.Builder;

@Builder
public record EventListResponse(
	Long id,
	String title,
	EventType type,
	EventAppType appType,
	EventStatus status,
	LocalDateTime eventAt,
	LocalDateTime appStartAt,
	LocalDateTime appEndAt,
	EventRegion region,
	String venue,
	List<CourseDto> courses,
	List<ImageDto> images,
	Long minPrice
) {

	@Builder
	public record CourseDto(
		Long id,
		String name,
		Integer distanceMeter,
		Long price
	) {
	}

	@Builder
	public record ImageDto(
		Long id,
		EventImageType type,
		String url,
		Integer sort
	) {
	}

	public static EventListResponse from(Event event) {
		List<CourseDto> courses = event.getCourses().stream()
			.map(course -> CourseDto.builder()
				.id(course.getId())
				.name(course.getName())
				.distanceMeter(course.getDistanceMeter())
				.price(course.getPrice())
				.build())
			.toList();

		List<ImageDto> images = event.getImages().stream()
			.filter(image -> image.getType() == EventImageType.THUMBNAIL)
			.sorted(Comparator.comparingInt(EventImage::getSort))
			.map(image -> ImageDto.builder()
				.id(image.getId())
				.type(image.getType())
				.url(image.getUrl())
				.sort(image.getSort())
				.build())
			.toList();

		Long minPrice = event.getCourses().stream()
			.mapToLong(EventCourse::getPrice)
			.min()
			.orElse(0L);

		return EventListResponse.builder()
			.id(event.getId())
			.title(event.getTitle())
			.type(event.getType())
			.appType(event.getAppType())
			.status(event.getStatus())
			.eventAt(event.getEventAt())
			.appStartAt(event.getAppStartAt())
			.appEndAt(event.getAppEndAt())
			.region(event.getRegion())
			.venue(event.getVenue())
			.courses(courses)
			.images(images)
			.minPrice(minPrice)
			.build();
	}
}
