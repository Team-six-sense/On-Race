package com.kt.onrace.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByLoginIdAndIsDeletedFalse(String loginId);
}
