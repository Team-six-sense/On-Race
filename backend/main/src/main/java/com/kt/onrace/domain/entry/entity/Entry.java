package com.kt.onrace.domain.entry.entity;

import com.kt.onrace.common.entity.BaseEntity;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventPace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "entry",
	// user_id와 event_id 중복 방지하기 위해 조합 유니크 설정
	uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"})
)
public class Entry extends BaseEntity {
	@Column(nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private EventCourse eventCourse;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pace_id", nullable = false)
	private EventPace eventPace;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EntryStatus status;

	@Builder
	private Entry(Long userId, Event event, EventCourse eventCourse, EventPace eventPace, EntryStatus status) {
		this.userId = userId;
		this.event = event;
		this.eventCourse = eventCourse;
		this.eventPace = eventPace;
		this.status = status;
	}

	public void updatePreSave(EventCourse eventCourse, EventPace eventPace) {
		this.eventCourse = eventCourse;
		this.eventPace = eventPace;
	}
}
