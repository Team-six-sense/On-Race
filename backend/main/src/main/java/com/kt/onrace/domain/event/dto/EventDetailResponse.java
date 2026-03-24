package com.kt.onrace.domain.event.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventImage;
import com.kt.onrace.domain.event.entity.EventImageType;
import com.kt.onrace.domain.event.entity.EventPace;

import lombok.Builder;

@Builder
public record EventDetailResponse(
	Long id,
	LocalDateTime lotteryAnnouncedAt,
	String notice,
	List<CourseDto> courses,
	List<PackageDto> packages,
	List<ImageDto> thumbnailImg,
	List<ImageDto> detailImg,
	DeliveryDto delivery
) {

	@Builder
	public record CourseDto(
		Long id,
		String name,
		String mapImg,
		int distanceMeter,
		long price,
		int courseCapacity,
		List<PaceDto> paces
	) {
	}

	@Builder
	public record PaceDto(
		Long id,
		String name,
		int hour,
		int minutes,
		int capacity
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

	@Builder
	public record ImageDto(
		Long id,
		String url
	) {
	}

	@Builder
	public record DeliveryDto(
		String schedule,
		String feePolicy
	) {
	}

	public static EventDetailResponse from(Event event, String deliverySchedule, String deliveryFeePolicy) {
		List<CourseDto> courses = event.getCourses().stream()
			.map(course -> CourseDto.builder()
				.id(course.getId())
				.name(course.getName())
				.mapImg(course.getMapUrl())
				.distanceMeter(course.getDistanceMeter())
				.price(course.getPrice())
				.courseCapacity(course.getEventPaces().stream()
					.mapToInt(EventPace::getCapacity)
					.sum())
				.paces(course.getEventPaces().stream()
					.map(pace -> PaceDto.builder()
						.id(pace.getId())
						.name(pace.getName())
						.hour(pace.getHour())
						.minutes(pace.getMinutes())
						.capacity(pace.getCapacity())
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

		Map<EventImageType, List<ImageDto>> imagesByType = event.getImages().stream()
			.sorted(Comparator.comparingInt(EventImage::getSort))
			.collect(Collectors.groupingBy(
				EventImage::getType,
				Collectors.mapping(
					image -> ImageDto.builder()
						.id(image.getId())
						.url(image.getUrl())
						.build(),
					Collectors.toList()
				)
			));

		List<ImageDto> thumbnailImg = imagesByType.getOrDefault(EventImageType.THUMBNAIL, Collections.emptyList());
		List<ImageDto> detailImg = imagesByType.getOrDefault(EventImageType.DETAIL, Collections.emptyList());

		DeliveryDto delivery = DeliveryDto.builder()
			.schedule(deliverySchedule)
			.feePolicy(deliveryFeePolicy)
			.build();

		return EventDetailResponse.builder()
			.id(event.getId())
			.lotteryAnnouncedAt(event.getLotteryAnnouncedAt())
			.notice(event.getNotice())
			.courses(courses)
			.packages(packages)
			.thumbnailImg(thumbnailImg)
			.detailImg(detailImg)
			.delivery(delivery)
			.build();
	}
}
