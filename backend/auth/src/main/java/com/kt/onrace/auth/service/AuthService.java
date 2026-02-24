package com.kt.onrace.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.ErrorCode;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.domain.auth.entity.User;
import com.kt.onrace.domain.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 회원가입: (1) email 중복 체크 (2) password BCrypt 해시 (3) User 저장
	 * 기본값: role=ROLE_USER, isDeleted=false
	 */
	@Transactional
	public SignupResult signup(String email, String rawPassword, String name, String mobile) {
		if (userRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.AUTH_DUPLICATE_EMAIL);
		}
		String encodedPassword = passwordEncoder.encode(rawPassword);
		User user = User.createForSignup(email, name, encodedPassword, mobile);
		User saved = userRepository.save(user);
		return new SignupResult(saved.getId(), saved.getEmail(), saved.getCreatedAt());
	}
}
