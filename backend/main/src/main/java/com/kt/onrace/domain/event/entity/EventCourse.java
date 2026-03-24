package com.kt.onrace.domain.event.entity;

import java.util.ArrayList;
import java.util.List;

import com.kt.onrace.common.entity.BaseEntity;

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

	@Column(length = 500)
	private String mapUrl;

	@Column(nullable = false)
	private int distanceMeter;

	@Column(nullable = false)
	private int timeLimit;

	@Column(nullable = false)
	private int waterSource;

	@Column(nullable = false)
	private int altitude;

	@Column(nullable = false, length = 500)
	private String courseRoute;

	@Column(nullable = false)
	private long price;

	@OneToMany(mappedBy = "eventCourse")
	private List<EventPace> eventPaces = new ArrayList<>();

	@Builder
	public EventCourse(Event event, String name, String mapUrl, Integer distanceMeter, Integer timeLimit,
		Integer waterSource, Integer altitude, String courseRoute, long price) {
		this.event = event;
		this.name = name;
		this.mapUrl = mapUrl;
		this.distanceMeter = distanceMeter;
		this.timeLimit = timeLimit;
		this.waterSource = waterSource;
		this.altitude = altitude;
		this.courseRoute = courseRoute;
		this.price = price;
	}
}
