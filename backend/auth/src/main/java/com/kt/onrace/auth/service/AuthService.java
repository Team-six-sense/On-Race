package com.kt.onrace.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.auth.api.dto.SignupRequest;
import com.kt.onrace.auth.api.dto.SignupResponse;
import com.kt.onrace.auth.domain.entity.User;
import com.kt.onrace.auth.domain.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.BusinessErrorCode;  // 여기 수정!

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		// 로그인 ID 중복 검사
		if (userRepository.existsByLoginId(request.getLoginId())) {
			throw new BusinessException(BusinessErrorCode.AUTH_DUPLICATE_LOGIN_ID);  // 수정!
		}

		// 이메일 중복 검사
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BusinessException(BusinessErrorCode.AUTH_DUPLICATE_EMAIL);  // 수정!
		}

		// 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(request.getPassword());

		// User 생성 (기존 팩토리 메서드 사용)
		User user = User.createUser(
			request.getLoginId(),
			request.getName(),
			encodedPassword,
			request.getEmail(),
			request.getMobile(),
			request.getGender()
		);

		User saved = userRepository.save(user);

		return new SignupResponse(
			saved.getId(),
			saved.getEmail(),
			saved.getCreatedAt()
		);
	}
}