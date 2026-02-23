package com.kt.onrace.domain.event.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.kt.onrace.domain.event.entity.EventType;

import lombok.Builder;

@Builder
public record EventDetailResponse(
	Long id,
	String title,
	EventStatus status,
	EventType type,
	String thumbnailImg,
	String detailImg,
	String courseMapImg,
	LocalDateTime eventAt,
	LocalDateTime appStartAt,
	LocalDateTime appEndAt,
	String venueAddress,
	LocalDateTime lotteryAnnouncedAt,
	String notice,
	List<CourseDto> courses,
	List<PackageDto> packages
) {

	@Builder
	public record CourseDto(
		Long id,
		String name,
		int distanceM,
		long price,
		List<PaceDto> paces
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

	@Builder
	public record PackageDto(
		Long id,
		String name,
		long price,
		String description
	) {
	}

	public static EventDetailResponse from(Event event) {
		List<CourseDto> courses = event.getCourses().stream()
			.map(course -> CourseDto.builder()
				.id(course.getId())
				.name(course.getName())
				.distanceM(course.getDistanceM())
				.price(course.getPrice())
				.paces(course.getEventPaces().stream()
					.map(pace -> PaceDto.builder()
						.id(pace.getId())
						.name(pace.getName())
						.hour(pace.getHour())
						.minutes(pace.getMinutes())
						.build())
					.toList())
				.build())
			.toList();

		List<PackageDto> packages = event.getPackages().stream()
			.map(pkg -> PackageDto.builder()
				.id(pkg.getId())
				.name(pkg.getName())
				.price(pkg.getPrice())
				.description(pkg.getDescription())
				.build())
			.toList();

		return EventDetailResponse.builder()
			.id(event.getId())
			.title(event.getTitle())
			.status(event.getStatus())
			.type(event.getType())
			.thumbnailImg(event.getThumbnailImg())
			.detailImg(event.getDetailImg())
			.courseMapImg(event.getCourseMapImg())
			.eventAt(event.getEventAt())
			.appStartAt(event.getAppStartAt())
			.appEndAt(event.getAppEndAt())
			.venueAddress(event.getVenueAddress())
			.lotteryAnnouncedAt(event.getLotteryAnnouncedAt())
			.notice(event.getNotice())
			.courses(courses)
			.packages(packages)
			.build();
	}
}
