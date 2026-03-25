package com.kt.onrace.domain.mypage.client;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;

@Component
public class HttpAuthAccountClient implements AuthAccountClient {

	private static final ParameterizedTypeReference<AuthApiResponse<AuthAccountPayload>> ACCOUNT_ME_RESPONSE_TYPE =
		new ParameterizedTypeReference<>() {
		};

	private final RestClient restClient;

	public HttpAuthAccountClient(
		RestClient.Builder restClientBuilder,
		@Value("${api.url.auth}") String authServiceUrl
	) {
		this.restClient = restClientBuilder
			.baseUrl(authServiceUrl)
			.build();
	}

	@Override
	public AccountSummary getMyInfo(Long userId) {
		try {
			AuthApiResponse<AuthAccountPayload> response = restClient.get()
				.uri("/account/me")
				.header("X-User-Id", String.valueOf(userId))
				.retrieve()
				.body(ACCOUNT_ME_RESPONSE_TYPE);

			if (response == null || !response.success() || response.data() == null) {
				throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
			}

			AuthAccountPayload data = response.data();
			return new AccountSummary(
				data.name(),
				data.phone(),
				data.authProvider(),
				data.verificationStatus(),
				data.marketingConsent()
			);
		} catch (RestClientResponseException exception) {
			int statusCode = exception.getStatusCode().value();
			if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
				throw new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER);
			}
			if (statusCode == HttpStatus.FORBIDDEN.value()) {
				throw new BusinessException(BusinessErrorCode.AUTH_FORBIDDEN_USER);
			}
			throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
		} catch (RestClientException exception) {
			throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
		}
	}

	private record AuthApiResponse<T>(
		boolean success,
		String code,
		String message,
		T data,
		LocalDateTime timestamp
	) {
	}

	private record AuthAccountPayload(
		String name,
		String phone,
		String authProvider,
		String verificationStatus,
		boolean marketingConsent
	) {
	}
}
