package com.kt.onrace.auth.common.oauth2;

import java.util.Map;

import com.kt.onrace.auth.common.oauth2.userinfo.KakaoOAuth2UserInfo;
import com.kt.onrace.auth.common.oauth2.userinfo.NaverOAuth2UserInfo;
import com.kt.onrace.auth.entity.AuthProvider;

public class OAuth2UserInfoFactory {

	public static OAuth2UserInfo of(AuthProvider provider, Map<String, Object> attributes) {
		return switch (provider) {
			case KAKAO -> new KakaoOAuth2UserInfo(attributes);
			case NAVER -> new NaverOAuth2UserInfo(attributes);
			default -> throw new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다: " + provider);
		};
	}
}
