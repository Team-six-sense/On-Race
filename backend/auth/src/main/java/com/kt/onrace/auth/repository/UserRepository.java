package com.kt.onrace.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.auth.entity.AuthProvider;
import com.kt.onrace.auth.entity.User;
import com.kt.onrace.auth.entity.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);

	boolean existsByPhoneNumber(String phoneNumber);

	Optional<User> findByEmailAndStatus(String email, UserStatus status);

	Optional<User> findByPhoneNumberAndStatus(String phoneNumber, UserStatus status);

	Optional<User> findByProviderIdAndAuthProvider(String providerId, AuthProvider authProvider);

}