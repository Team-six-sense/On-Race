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
		Integer timeLimit,
		Integer waterSource,
		Integer altitude,
		String courseRoute,
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
				.timeLimit(course.getTimeLimit())
				.waterSource(course.getWaterSource())
				.altitude(course.getAltitude())
				.courseRoute(course.getCourseRoute())
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

		// 와이어 프레임에는 패키지 정보가 노출이 되었으나 해당 부분은 상세 이미지에서 노출이 되는 것으로 변경되어 패키지 정보는 제외하였습니다.
		/*List<PackageDto> packages = event.getPackages().stream()
			.collect(Collectors.groupingBy(
				EventPackage::getItemType,
				Collectors.mapping(
					eventPackage -> {
						EventItem item = eventPackage.getItem();
						List<OptionDto> sizes = item.getSizes().stream()
							.map(size -> OptionDto.builder()
								.id(size.getId())
								.option(size.getOption())
								.build())
							.toList();

						return ItemDto.builder()
							.id(item.getId())
							.name(item.getName())
							.price(item.getPrice())
							.description(item.getDescription())
							.sizes(sizes)
							.build();
					},
					Collectors.toList()
				)
			))
			.entrySet().stream()
			.map(entry -> PackageDto.builder()
				.itemType(entry.getKey().name())
				.items(entry.getValue())
				.build())
			.toList();*/

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
			.thumbnailImg(thumbnailImg)
			.detailImg(detailImg)
			.delivery(delivery)
			.build();
	}
}
