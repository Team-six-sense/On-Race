package com.kt.onrace.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.auth.config.AppProperties;
import com.kt.onrace.auth.dto.AccountMeResponse;
import com.kt.onrace.auth.entity.AuthProvider;
import com.kt.onrace.auth.entity.User;
import com.kt.onrace.auth.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AppProperties appProperties;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetService passwordResetService;

	public AccountMeResponse getMyInfo(Long userId) {
		User user = findActiveUser(userId);
		return new AccountMeResponse(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getAuthProvider(),
			user.getStatus(),
			user.getPhoneNumber(),
			user.canChangePassword(),
			user.getVerificationStatus(),
			user.isMarketingConsent()
		);
	}

	@Transactional
	public void updateName(Long userId, String name) {
		User user = findActiveUser(userId);
		user.changeName(name);
	}

	public void requestPasswordChange(Long userId, String currentPassword) {
		User user = findActiveUser(userId);

		if (user.getAuthProvider() != AuthProvider.LOCAL) {
			throw new BusinessException(BusinessErrorCode.AUTH_FORBIDDEN_USER);
		}

		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_PASSWORD);
		}

		passwordResetService.requestPasswordReset(user.getEmail(), appProperties.getPasswordReset().getBaseUrl());
	}

	@Transactional
	public void updateMarketingConsent(Long userId, boolean marketingConsent) {
		User user = findActiveUser(userId);
		user.changeMarketingConsent(marketingConsent);
	}

	private User findActiveUser(Long userId) {
		return userRepository.findById(userId)
			.filter(User::isActive)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER));
	}
}
