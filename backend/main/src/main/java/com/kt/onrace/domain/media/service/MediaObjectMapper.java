package com.kt.onrace.domain.media.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.kt.onrace.domain.media.entity.MediaObject;

@Component
public class MediaObjectMapper {

	public MediaObject toEntity(Long ownerId, String objectKey, String contentType, LocalDateTime expiresAt) {
		return MediaObject.builder()
			.ownerId(ownerId)
			.objectKey(objectKey)
			.contentType(contentType)
			.expiresAt(expiresAt)
			.build();
	}

}
