package com.kt.onrace.auth.service;

import java.util.Date;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.auth.common.client.MainServiceClient;
import com.kt.onrace.auth.dto.LoginRequest;
import com.kt.onrace.auth.dto.LoginResponse;
import com.kt.onrace.auth.dto.SignupRequest;
import com.kt.onrace.auth.dto.SignupResponse;
import com.kt.onrace.auth.dto.TokenRefreshRequest;
import com.kt.onrace.auth.dto.TokenRefreshResponse;
import com.kt.onrace.auth.dto.WithdrawRequest;
import com.kt.onrace.auth.entity.User;
import com.kt.onrace.auth.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.security.JwtProperties;
import com.kt.onrace.common.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final MainServiceClient mainServiceClient;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtProperties jwtProperties;
	private final TokenStoreService tokenStoreService;

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException(BusinessErrorCode.AUTH_DUPLICATE_EMAIL);
		}

		String encodedPassword = passwordEncoder.encode(request.password());

		User user = User.createUser(
			request.email(),
			encodedPassword
		);

		User saved = userRepository.save(user);

		mainServiceClient.syncUserCreated(saved.getId());

		return new SignupResponse(saved.getId(), saved.getEmail(), saved.getCreatedAt());
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmailAndIsDeletedFalse(request.email())
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_PASSWORD);
		}

		String accessToken = jwtTokenProvider.generateAccessToken(
			user.getId(), user.getEmail(), user.getRole().name());
		String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

		tokenStoreService.saveRefreshToken(
			user.getId(), refreshToken, jwtProperties.getRefreshTokenExpiration());

		return new LoginResponse(accessToken, refreshToken, "Bearer", jwtProperties.getAccessTokenExpiration());
	}

	@Transactional(readOnly = true)
	public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
		String refreshToken = request.refreshToken();

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
			user.getId(), user.getEmail(), user.getRole().name());

		return new TokenRefreshResponse(newAccessToken, jwtProperties.getAccessTokenExpiration());
	}

	public void logout(Long userId, String accessToken) {
		String jti = jwtTokenProvider.getJti(accessToken);
		Date expiration = jwtTokenProvider.getExpiration(accessToken);
		long remainingTtlMs = expiration.getTime() - System.currentTimeMillis();

		if (remainingTtlMs > 0) {
			tokenStoreService.blacklistJti(jti, remainingTtlMs);
		}

		tokenStoreService.deleteRefreshToken(userId);
	}

	@Transactional
	public void withdraw(Long userId, String accessToken, WithdrawRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER));

		if (user.isDeleted()) {
			throw new BusinessException(BusinessErrorCode.AUTH_ALREADY_WITHDRAWN);
		}

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_PASSWORD);
		}

		user.markDeleted();
		mainServiceClient.syncUserDeleted(userId);
		
		logout(userId, accessToken);
	}
}
