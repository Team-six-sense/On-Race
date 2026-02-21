package com.kt.onrace.domain.event.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.kt.onrace.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event")
public class Event extends BaseEntity {

	@Column(nullable = false, length = 50)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventStatus status;

	@Column(nullable = false, length = 500)
	private String thumbnailImg;

	@Column(nullable = false, length = 500)
	private String detailImg;

	@Column(nullable = false, length = 500)
	private String courseMapImg;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventType type;

	@Column(nullable = false)
	private LocalDateTime eventAt;

	@Column(nullable = false)
	private LocalDateTime appStartAt;

	@Column(nullable = false)
	private LocalDateTime appEndAt;

	@Column(nullable = false)
	private String venueAddress;

	private LocalDateTime lotteryAnnouncedAt;

	@Column(columnDefinition = "TEXT")
	private String notice;

	@Column(nullable = false)
	private boolean isView = false;

	@Column(nullable = false)
	private boolean isDeleted = false;

	@OneToMany(mappedBy = "event")
	private List<EventCourse> courses = new ArrayList<>();

	@OneToMany(mappedBy = "event")
	private List<EventPackage> packages = new ArrayList<>();

	@Builder
	public Event(String title, EventStatus status, String thumbnailImg, String detailImg,
			String courseMapImg, EventType type, LocalDateTime eventAt,
			LocalDateTime appStartAt, LocalDateTime appEndAt, String venueAddress,
			LocalDateTime lotteryAnnouncedAt, String notice, Boolean isView) {
		this.title = title;
		this.status = status;
		this.thumbnailImg = thumbnailImg;
		this.detailImg = detailImg;
		this.courseMapImg = courseMapImg;
		this.type = type;
		this.eventAt = eventAt;
		this.appStartAt = appStartAt;
		this.appEndAt = appEndAt;
		this.venueAddress = venueAddress;
		this.lotteryAnnouncedAt = lotteryAnnouncedAt;
		this.notice = notice;
		this.isView = isView != null ? isView : false;
		this.isDeleted = false;
	}

	public void update(String title, EventStatus status, String thumbnailImg, String detailImg,
			String courseMapImg, EventType type, LocalDateTime eventAt,
			LocalDateTime appStartAt, LocalDateTime appEndAt, String venueAddress,
			LocalDateTime lotteryAnnouncedAt, String notice, Boolean isView) {
		this.title = title;
		this.status = status;
		this.thumbnailImg = thumbnailImg;
		this.detailImg = detailImg;
		this.courseMapImg = courseMapImg;
		this.type = type;
		this.eventAt = eventAt;
		this.appStartAt = appStartAt;
		this.appEndAt = appEndAt;
		this.venueAddress = venueAddress;
		this.lotteryAnnouncedAt = lotteryAnnouncedAt;
		this.notice = notice;
		this.isView = isView != null ? isView : this.isView;
	}

	public void delete() {
		this.isDeleted = true;
	}
}
