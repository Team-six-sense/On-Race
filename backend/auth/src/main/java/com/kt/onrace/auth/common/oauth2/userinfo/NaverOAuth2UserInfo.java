package com.kt.onrace.auth.common.oauth2.userinfo;

import java.util.Map;

import com.kt.onrace.auth.common.oauth2.OAuth2UserInfo;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {

	private final Map<String, Object> attributes;

	@SuppressWarnings("unchecked")
	public NaverOAuth2UserInfo(Map<String, Object> attributes) {
		// Naver 응답: { resultcode, message, response: { id, email, name, mobile } }
		this.attributes = (Map<String, Object>) attributes.get("response");
	}

	@Override
	public String getProviderId() {
		return (String) attributes.get("id");
	}

	@Override
	public String getEmail() {
		return (String) attributes.get("email");
	}

	@Override
	public String getName() {
		return (String) attributes.get("name");
	}
}
