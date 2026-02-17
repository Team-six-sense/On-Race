package com.kt.onrace.domain.event.entity;

import java.util.ArrayList;
import java.util.List;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_course")
public class EventCourse extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private int distanceM;

	@Column(nullable = false)
	private long price;

	@OneToMany(mappedBy = "eventCourse")
	private List<EventPace> eventPaces = new ArrayList<>();

	@Builder
	public EventCourse(Event event, String name, Integer distanceM, Long price) {
		this.event = event;
		this.name = name;
		this.distanceM = distanceM;
		this.price = price;
	}
}
