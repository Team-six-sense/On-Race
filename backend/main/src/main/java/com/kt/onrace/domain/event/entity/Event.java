package com.kt.onrace.domain.event.entity;

import java.math.BigDecimal;
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
import jakarta.persistence.Transient;
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
	private EventType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventAppType appType;

	@Column(nullable = false)
	private LocalDateTime eventAt;

	@Column(nullable = false)
	private LocalDateTime appStartAt;

	@Column(nullable = false)
	private LocalDateTime appEndAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventRegion region; //  지역명: SEOUL

	@Column(nullable = false)
	private String venue; //  장소명: 서울 올림픽 공원

	private LocalDateTime lotteryAnnouncedAt;

	private BigDecimal discountRate;

	@Column(columnDefinition = "TEXT")
	private String notice;

	@Column(nullable = false)
	private boolean isView = false;

	@Column(nullable = false)
	private boolean isDeleted = false;

	@Column(nullable = false)
	private boolean soldOut = false;

	@Column(nullable = false)
	private boolean isQueue = false;

	@OneToMany(mappedBy = "event")
	private List<EventCourse> courses = new ArrayList<>();

	@OneToMany(mappedBy = "event")
	private List<EventPackage> packages = new ArrayList<>();

	@OneToMany(mappedBy = "event")
	private List<EventImage> images = new ArrayList<>();

	@Transient // 이것은 컬럼이 아님을 뜻
	public EventStatus getStatus() {
		return EventStatus.resolveStatus(this.appType, this.appStartAt, this.appEndAt, this.lotteryAnnouncedAt, this.soldOut);
	}

	@Builder
	public Event(String title, EventType type, EventAppType appType, LocalDateTime eventAt,
		LocalDateTime appStartAt, LocalDateTime appEndAt, EventRegion region, String venue,
		LocalDateTime lotteryAnnouncedAt, BigDecimal discountRate, String notice, Boolean isView, Boolean soldOut) {
		this.title = title;
		this.type = type;
		this.appType = appType;
		this.eventAt = eventAt;
		this.appStartAt = appStartAt;
		this.appEndAt = appEndAt;
		this.region = region;
		this.venue = venue;
		this.discountRate = discountRate;
		this.lotteryAnnouncedAt = lotteryAnnouncedAt;
		this.notice = notice;
		this.isView = isView != null ? isView : false;
		this.isDeleted = false;
		this.soldOut = soldOut != null ? soldOut : false;
	}

	public void update(String title, EventType type, EventAppType appType, LocalDateTime eventAt,
		LocalDateTime appStartAt, LocalDateTime appEndAt, EventRegion region, String venue,
		LocalDateTime lotteryAnnouncedAt, BigDecimal discountRate, String notice, Boolean isView, Boolean soldOut) {
		this.title = title;
		this.type = type;
		this.appType = appType;
		this.eventAt = eventAt;
		this.appStartAt = appStartAt;
		this.appEndAt = appEndAt;
		this.region = region;
		this.venue = venue;
		this.lotteryAnnouncedAt = lotteryAnnouncedAt;
		this.discountRate = discountRate;
		this.notice = notice;
		this.isView = isView != null ? isView : this.isView;
		this.soldOut = soldOut != null ? soldOut : this.soldOut;
	}

	public void enableQueue() {
		this.isQueue = true;
	}

	public void disableQueue() {
		this.isQueue = false;
	}
}
