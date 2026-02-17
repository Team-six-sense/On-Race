package com.kt.onrace.domain.event.entity;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_pace")
public class EventPace extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private EventCourse eventCourse;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private int hour = 0;

	@Column(nullable = false)
	private int minutes = 0;

	@Builder
	public EventPace(EventCourse eventCourse, String name, Byte hour, Byte minutes) {
		this.eventCourse = eventCourse;
		this.name = name;
		this.hour = hour != null ? hour : 0;
		this.minutes = minutes != null ? minutes : 0;
	}
}
