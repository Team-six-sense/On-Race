package com.kt.onrace.domain.media.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.media.dto.ConfirmUploadResponseDto;
import com.kt.onrace.domain.media.dto.PresignUploadRequestDto;
import com.kt.onrace.domain.media.dto.PresignUploadResponseDto;
import com.kt.onrace.domain.media.entity.MediaObject;
import com.kt.onrace.domain.media.entity.MediaStatus;
import com.kt.onrace.domain.media.repository.MediaObjectRepository;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class MediaService {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"image/jpeg", "image/png", "image/webp"
	);

	private final S3Presigner s3Presigner;
	private final S3Client s3Client;

	private final MediaObjectMapper mediaObjectMapper;
	private final MediaObjectRepository mediaObjectRepository;

	@Value("${aws.s3.bucket}")
	private String bucket;

	@Value("${aws.s3.presign-expire-seconds}")
	private Long presignExpireSeconds;

	public PresignUploadResponseDto issueUploadUrl(Long userId, PresignUploadRequestDto request) {
		validateContentType(request.contentType());

		String objectKey = buildObjectKey(userId, request.filename());

		Duration ttl = Duration.ofSeconds(presignExpireSeconds);

		MediaObject mediaObject = mediaObjectMapper.toEntity(
			userId,
			objectKey,
			request.contentType(),
			LocalDateTime.now().plusSeconds(ttl.getSeconds())
		);

		mediaObjectRepository.save(mediaObject);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(objectKey)
			.contentType(request.contentType())
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(ttl)
			.putObjectRequest(putObjectRequest)
			.build();

		PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

		return PresignUploadResponseDto.builder()
			.mediaObjectId(mediaObject.getId())
			.objectKey(objectKey)
			.uploadUrl(presigned.url().toString())
			.expiresIn(ttl.getSeconds())
			.build();
	}

	@Transactional
	public ConfirmUploadResponseDto confirmUpload(Long userId, Long mediaId) {
		MediaObject mediaObject = mediaObjectRepository.findByIdAndOwnerId(mediaId, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.MEDIA_NOT_FOUND));

		if (mediaObject.getMediaStatus() == MediaStatus.UPLOADED) {
			return ConfirmUploadResponseDto.builder()
				.mediaId(mediaObject.getId())
				.status(mediaObject.getMediaStatus().name())
				.build();
		}

		try {
			HeadObjectResponse headObjectResponse = s3Client.headObject(HeadObjectRequest.builder()
				.bucket(bucket)
				.key(mediaObject.getObjectKey())
				.build());

			if (headObjectResponse.contentType() == null) {
				mediaObject.markDeleted();
				throw new BusinessException(BusinessErrorCode.MEDIA_CONFIRM_FAILED);
			}

			mediaObject.markUploaded(LocalDateTime.now());

			return ConfirmUploadResponseDto.builder()
				.mediaId(mediaObject.getId())
				.status(mediaObject.getMediaStatus().name())
				.build();
		} catch (NoSuchKeyException e) {

			mediaObject.markDeleted();
			throw new BusinessException(BusinessErrorCode.MEDIA_OBJECT_NOT_FOUND);

		} catch (Exception e) {

			throw new BusinessException(BusinessErrorCode.MEDIA_CONFIRM_FAILED);

		}
	}

	private void validateContentType(String contentType) {
		if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new BusinessException(BusinessErrorCode.MEDIA_UNSUPPORTED_CONTENT_TYPE);
		}
	}

	private String buildObjectKey(Long userId, String filename) {
		String extension = extractExtension(filename);

		LocalDate now = LocalDate.now();

		return String.format(
			"users/%d/%d/%02d/%02d/%s.%s",
			userId, now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
			UUID.randomUUID(), extension
		);
	}

	private String extractExtension(String filename) {
		if (filename == null || filename.contains("/") || filename.contains("\\")) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_FORMAT);
		}

		int lastDot = filename.lastIndexOf('.');

		if (lastDot == -1 || lastDot == 0) {
			return "";
		}

		return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
	}

}
