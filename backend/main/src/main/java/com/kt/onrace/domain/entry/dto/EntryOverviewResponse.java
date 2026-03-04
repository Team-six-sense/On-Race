package com.kt.onrace.domain.entry.dto;

import java.util.List;

import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventPace;

import lombok.Builder;

@Builder
public record EntryOverviewResponse(
	boolean hasEntry,
	EntryDto entry,
	List<CourseOptionDto> courses,
	RateInfoDto rateInfo
) {

	@Builder
	public record EntryDto(
		Long id,
		EntryStatus status,
		Long selectedCourseId,
		Long selectedPaceId
	) {

		public static EntryDto from(Entry entry) {
			return EntryDto.builder()
				.id(entry.getId())
				.status(entry.getStatus())
				.selectedCourseId(entry.getEventCourse().getId())
				.selectedPaceId(entry.getEventPace().getId())
				.build();
		}
	}

	@Builder
	public record CourseOptionDto(
		Long id,
		String name,
		int distanceM,
		long price,
		List<PaceOptionDto> paces
	) {

		public static CourseOptionDto from(EventCourse course) {
			return CourseOptionDto.builder()
				.id(course.getId())
				.name(course.getName())
				.distanceM(course.getDistanceM())
				.price(course.getPrice())
				.paces(course.getEventPaces().stream()
					.map(PaceOptionDto::from)
					.toList())
				.build();
		}
	}

	@Builder
	public record PaceOptionDto(
		Long id,
		String name,
		int hour,
		int minutes,
		int capacity
	) {

		public static PaceOptionDto from(EventPace pace) {
			return PaceOptionDto.builder()
				.id(pace.getId())
				.name(pace.getName())
				.hour(pace.getHour())
				.minutes(pace.getMinutes())
				.capacity(pace.getCapacity())
				.build();
		}
	}

	public record RateInfoDto(
		long entryCount,
		int capacity,
		long competitionRate,
		long fillRatePercent,
		long price
	) {

		public static RateInfoDto of(long totalCount, long appliedCount, int capacity, long price) {
			long competitionRate = 0;
			long fillRatePercent = 0;

			if (capacity > 0) {
				competitionRate = Math.round((double) totalCount / capacity * 100);
				fillRatePercent = Math.min(Math.round((double) appliedCount / capacity * 100), 100);
			}

			return new RateInfoDto(totalCount, capacity, competitionRate, fillRatePercent, price);
		}
	}
}
