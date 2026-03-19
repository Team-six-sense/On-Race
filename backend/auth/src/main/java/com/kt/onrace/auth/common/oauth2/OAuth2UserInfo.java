package com.kt.onrace.auth.common.oauth2;

public interface OAuth2UserInfo {

	String getProviderId();

	String getEmail();

	String getName();
}
