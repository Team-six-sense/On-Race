package com.kt.onrace.auth.controller;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.kt.onrace.auth.dto.SmsSendRequest;
import com.kt.onrace.auth.service.SmsVerifyService;

@ExtendWith(MockitoExtension.class)
class SmsControllerFindEmailTest {

	@InjectMocks
	private SmsController smsController;

	@Mock
	private SmsVerifyService smsVerifyService;

	// ── IP 추출 로직 ─────────────────────────────────────────────

	@Test
	@DisplayName("X-Forwarded-For 단일 IP: 해당 IP를 클라이언트 IP로 사용")
	void sendCodeForFind_xForwardedFor_singleIp() {
		SmsSendRequest request = new SmsSendRequest("01012345678");
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addHeader("X-Forwarded-For", "203.0.113.1");
		httpRequest.setRemoteAddr("10.0.0.1");

		smsController.sendCodeForFind(request, httpRequest);

		then(smsVerifyService).should().sendCodeForFind("01012345678", "203.0.113.1");
	}

	@Test
	@DisplayName("X-Forwarded-For 체이닝: 첫 번째 IP(원본 클라이언트)를 사용")
	void sendCodeForFind_xForwardedFor_chainedIps_usesFirst() {
		SmsSendRequest request = new SmsSendRequest("01012345678");
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.2, 10.0.0.3");
		httpRequest.setRemoteAddr("10.0.0.1");

		smsController.sendCodeForFind(request, httpRequest);

		then(smsVerifyService).should().sendCodeForFind("01012345678", "203.0.113.1");
	}

	@Test
	@DisplayName("X-Forwarded-For 없음: getRemoteAddr() 폴백")
	void sendCodeForFind_noXForwardedFor_usesRemoteAddr() {
		SmsSendRequest request = new SmsSendRequest("01012345678");
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.setRemoteAddr("10.0.0.5");

		smsController.sendCodeForFind(request, httpRequest);

		then(smsVerifyService).should().sendCodeForFind("01012345678", "10.0.0.5");
	}
}
