package com.kt.onrace.auth.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.auth.common.oauth2.OAuth2UserInfo;
import com.kt.onrace.auth.common.oauth2.OAuth2UserInfoFactory;
import com.kt.onrace.auth.common.oauth2.OAuth2UserPrincipal;
import com.kt.onrace.auth.entity.AuthProvider;
import com.kt.onrace.auth.entity.User;
import com.kt.onrace.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(request);

		String registrationId = request.getClientRegistration().getRegistrationId();
		AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

		OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(provider, oAuth2User.getAttributes());

		User user = userRepository.findByProviderIdAndAuthProvider(userInfo.getProviderId(), provider)
				.orElseGet(() -> registerOAuthUser(userInfo, provider));

		return new OAuth2UserPrincipal(user.getId(), user.getEmail(), user.getRole().name(),
				oAuth2User.getAttributes());
	}

	private User registerOAuthUser(OAuth2UserInfo userInfo, AuthProvider provider) {
		// 동일 이메일로 LOCAL 계정이 이미 존재하는 경우 소셜 로그인 차단
		if (userInfo.getEmail() != null) {
			userRepository.findByEmailAndIsDeletedFalse(userInfo.getEmail()).ifPresent(existing -> {
				throw new OAuth2AuthenticationException(
						new OAuth2Error("email_already_registered",
								"이미 일반 회원가입으로 등록된 이메일입니다. 이메일/비밀번호로 로그인해 주세요.", null));
			});
		}

		User newUser = User.createOAuthUser(userInfo.getEmail(), userInfo.getName(), provider,
				userInfo.getProviderId());
		return userRepository.save(newUser);
	}
}
