package com.kt.onrace.domain.mypage.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.response.ApiResponse;

/**
 * auth 서비스에서 마이페이지 계정 원천 데이터를 읽어오는 내부 클라이언트이다.
 * 내부 신뢰 구간 호출이므로 X-User-Id 와 X-Gateway-Token 을 함께 전달한다.
 */
@Component
public class AuthClient {

	private final RestTemplate restTemplate;
	private final String authBaseUrl;
	private final String gatewaySecret;

	public AuthClient(
		RestTemplateBuilder restTemplateBuilder,
		@Value("${api.url.auth:http://localhost:8081}") String authBaseUrl,
		@Value("${gateway.internal.secret}") String gatewaySecret
	) {
		this.restTemplate = restTemplateBuilder.build();
		this.authBaseUrl = authBaseUrl;
		this.gatewaySecret = gatewaySecret;
	}

	public AuthAccountResponse getAccount(Long userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-User-Id", String.valueOf(userId));
		headers.set("X-Gateway-Token", gatewaySecret);
		headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

		try {
			ResponseEntity<ApiResponse<AuthAccountResponse>> response = restTemplate.exchange(
				authBaseUrl + "/account/me",
				HttpMethod.GET,
				new HttpEntity<>(headers),
				new ParameterizedTypeReference<ApiResponse<AuthAccountResponse>>() {
				}
			);

			ApiResponse<AuthAccountResponse> body = response.getBody();
			if (body == null || !body.isSuccess() || body.getData() == null) {
				throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
			}

			return body.getData();
		} catch (RestClientException exception) {
			throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
		}
	}

	public record AuthAccountResponse(
		Long id,
		String email,
		String name,
		String phone,
		boolean canChangePassword,
		String verificationStatus,
		boolean marketingConsent
	) {
	}
}
