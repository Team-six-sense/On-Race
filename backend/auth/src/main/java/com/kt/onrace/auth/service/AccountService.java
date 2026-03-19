package com.kt.onrace.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.auth.dto.AccountMeResponse;
import com.kt.onrace.auth.entity.User;
import com.kt.onrace.auth.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final UserRepository userRepository;

	public AccountMeResponse getMyInfo(Long userId) {
		User user = findActiveUser(userId);
		return new AccountMeResponse(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getAuthProvider(),
			user.getStatus()
		);
	}

	@Transactional
	public void updateName(Long userId, String name) {
		User user = findActiveUser(userId);
		user.changeName(name);
	}

	private User findActiveUser(Long userId) {
		return userRepository.findById(userId)
			.filter(User::isActive)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER));
	}
}
