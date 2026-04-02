package com.kt.onrace.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaptchaVerifyService {

	private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

	@Value("${recaptcha.secret-key}")
	private String secretKey;

	private final RestClient restClient;

	public boolean verify(String token) {
		if (token == null || token.isBlank()) {
			return false;
		}

		RecaptchaResponse response = restClient.post()
				.uri(VERIFY_URL)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body("secret=" + secretKey + "&response=" + token)
				.retrieve()
				.body(RecaptchaResponse.class);

		return response != null && response.success();
	}

	private record RecaptchaResponse(boolean success) {}
}
