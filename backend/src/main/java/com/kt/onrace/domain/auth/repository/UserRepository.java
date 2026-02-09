package com.kt.onrace.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.domain.auth.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByLoginIdAndIsDeletedFalse(String loginId);
}
