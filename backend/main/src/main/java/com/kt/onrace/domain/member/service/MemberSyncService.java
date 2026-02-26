package com.kt.onrace.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.member.entity.Member;
import com.kt.onrace.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberSyncService {

	private final MemberRepository memberRepository;

	@Transactional
	public void syncUserCreated(Long userId) {
		if (memberRepository.existsById(userId)) {
			throw new BusinessException(BusinessErrorCode.AUTH_DUPLICATE_USER_ID);
		}

		Member newMember = Member.createMember(userId);

		memberRepository.save(newMember);
	}

	@Transactional
	public void syncUserDeleted(Long authUserId) {
		Member member = memberRepository.findById(authUserId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER));

		member.markAsDeleted();

	}

}
