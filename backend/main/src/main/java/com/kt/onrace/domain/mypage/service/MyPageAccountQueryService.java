package com.kt.onrace.domain.mypage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.domain.address.service.AddressService;
import com.kt.onrace.domain.mypage.client.AuthAccountClient;
import com.kt.onrace.domain.mypage.dto.MyPageAccountResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * 마이페이지 회원정보 화면에서 필요한 계정 원천값과 배송지 목록을 조합하는 서비스이다.
 */
@ServiceLog
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageAccountQueryService {

	private final AuthAccountClient authAccountClient;
	private final AddressService addressService;

	public MyPageAccountResponseDto getAccount(Long userId) {
		AuthAccountClient.AccountSummary account = authAccountClient.getMyInfo(userId);
		return MyPageAccountResponseDto.from(userId, account, addressService.list(userId));
	}
}
