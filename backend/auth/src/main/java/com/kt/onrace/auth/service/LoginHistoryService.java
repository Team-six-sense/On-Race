package com.kt.onrace.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.auth.entity.LoginHistory;
import com.kt.onrace.auth.repository.LoginHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

	private final LoginHistoryRepository loginHistoryRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordSuccess(Long userId, String loginIp, String loginAgent) {
		loginHistoryRepository.save(LoginHistory.success(userId, loginIp, loginAgent));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordFail(String loginIp, String loginAgent, String failReason) {
		loginHistoryRepository.save(LoginHistory.fail(loginIp, loginAgent, failReason));
	}
}
