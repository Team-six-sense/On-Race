package com.kt.onrace.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.auth.api.dto.LoginRequest;
import com.kt.onrace.auth.api.dto.LoginResponse;
import com.kt.onrace.auth.api.dto.SignupRequest;
import com.kt.onrace.auth.api.dto.SignupResponse;
import com.kt.onrace.auth.api.dto.TokenRefreshRequest;
import com.kt.onrace.auth.api.dto.TokenRefreshResponse;
import com.kt.onrace.auth.domain.entity.User;
import com.kt.onrace.auth.domain.repository.UserRepository;
import com.kt.onrace.auth.infrastructure.TokenStoreService;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.security.JwtProperties;
import com.kt.onrace.common.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtProperties jwtProperties;
	private final TokenStoreService tokenStoreService;

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		if (userRepository.existsByLoginId(request.getLoginId())) {
			throw new BusinessException(BusinessErrorCode.AUTH_DUPLICATE_LOGIN_ID);
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BusinessException(BusinessErrorCode.AUTH_DUPLICATE_EMAIL);
		}

		String encodedPassword = passwordEncoder.encode(request.getPassword());

		User user = User.createUser(
			request.getLoginId(),
			request.getName(),
			encodedPassword,
			request.getEmail(),
			request.getMobile(),
			request.getGender()
		);

		User saved = userRepository.save(user);

		return new SignupResponse(saved.getId(), saved.getEmail(), saved.getCreatedAt());
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByLoginIdAndIsDeletedFalse(request.getLoginId())
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_PASSWORD);
		}

		String accessToken = jwtTokenProvider.generateAccessToken(
			user.getId(), user.getLoginId(), user.getRole().name());
		String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

		tokenStoreService.saveRefreshToken(
			user.getId(), refreshToken, jwtProperties.getRefreshTokenExpiration());

		return new LoginResponse(accessToken, refreshToken, "Bearer", jwtProperties.getAccessTokenExpiration());
	}

	@Transactional(readOnly = true)
	public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
		String refreshToken = request.getRefreshToken();

		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_REFRESH_TOKEN);
		}

		Long userId = jwtTokenProvider.getUserId(refreshToken);

		String stored = tokenStoreService.getRefreshToken(userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.AUTH_INVALID_REFRESH_TOKEN));

		if (!stored.equals(refreshToken)) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_REFRESH_TOKEN);
		}

		User user = userRepository.findById(userId)
			.filter(u -> !u.isDeleted())
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER));

		String newAccessToken = jwtTokenProvider.generateAccessToken(
			user.getId(), user.getLoginId(), user.getRole().name());

		return new TokenRefreshResponse(newAccessToken, jwtProperties.getAccessTokenExpiration());
	}
}
