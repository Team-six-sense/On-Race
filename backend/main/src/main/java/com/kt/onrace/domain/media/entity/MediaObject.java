package com.kt.onrace.domain.media.entity;

import java.time.LocalDateTime;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "media_objects",
	indexes = {
		@Index(name = "index_media_status_expires", columnList = "media_status,expires_at")
	}
)
public class MediaObject extends BaseEntity {

	@Column(nullable = false)
	private Long ownerId;

	@Column(nullable = false, unique = true, length = 512)
	private String objectKey;

	@Column(nullable = false, length = 100)
	private String contentType;

	@Enumerated(EnumType.STRING)
	private MediaStatus mediaStatus;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	private LocalDateTime confirmedAt;

	@Builder
	private MediaObject(Long ownerId, String objectKey, String contentType, LocalDateTime expiresAt) {
		this.ownerId = ownerId;
		this.objectKey = objectKey;
		this.contentType = contentType;
		this.expiresAt = expiresAt;
		this.mediaStatus = MediaStatus.PRESIGNED;
	}

	public void markUploaded(LocalDateTime confirmedAt) {
		this.mediaStatus = MediaStatus.UPLOADED;
		this.confirmedAt = confirmedAt;
	}

	public void markDeleted() {
		this.mediaStatus = MediaStatus.DELETED;
	}

}
