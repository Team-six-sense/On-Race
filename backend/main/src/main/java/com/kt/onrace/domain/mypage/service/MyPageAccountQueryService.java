package com.kt.onrace.domain.mypage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.domain.mypage.client.AuthAccountClient;
import com.kt.onrace.domain.mypage.dto.MyPageAccountResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageAccountType;
import com.kt.onrace.domain.mypage.dto.MyPageVerificationStatus;

import lombok.RequiredArgsConstructor;

/**
 * 마이페이지 계정관리 첫 화면에서 필요한 계정 원천값과 기본 배송지 요약을 조합하는 서비스이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageAccountQueryService {

	private final AuthAccountClient authAccountClient;
	private final MyPageAddressQueryService myPageAddressQueryService;

	public MyPageAccountResponseDto getAccount(Long userId) {
		AuthAccountClient.AccountSummary account = authAccountClient.getMyInfo(userId);

		return new MyPageAccountResponseDto(
			MyPageAccountType.fromAuthProvider(account.authProvider()),
			account.authProvider(),
			account.email(),
			account.name(),
			account.phone(),
			account.canChangePassword(),
			MyPageVerificationStatus.fromAuthStatus(account.verificationStatus()),
			account.marketingConsent(),
			myPageAddressQueryService.getAddress(userId)
		);
	}
}
