package com.kt.onrace.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.ErrorCode;
import com.kt.onrace.domain.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	default Member findByIdAndIsDeletedFalseOrThrow(Long id, ErrorCode errorCode) {
		return findByIdAndIsDeletedFalse(id).orElseThrow(() -> new BusinessException(errorCode));
	}

	Optional<Member> findByIdAndIsDeletedFalse(Long id);
	
}
