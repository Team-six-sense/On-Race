package com.kt.onrace.domain.media.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.domain.media.entity.MediaObject;

public interface MediaObjectRepository extends JpaRepository<MediaObject, Long> {

	Optional<MediaObject> findByIdAndOwnerId(Long id, Long ownerId);

}
