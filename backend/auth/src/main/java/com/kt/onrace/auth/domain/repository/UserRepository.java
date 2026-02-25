package com.kt.onrace.auth.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.auth.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByLoginId(String loginId);

	boolean existsByEmail(String email);

	Optional<User> findByLoginId(String loginId);

	Optional<User> findByLoginIdAndIsDeletedFalse(String loginId);

}