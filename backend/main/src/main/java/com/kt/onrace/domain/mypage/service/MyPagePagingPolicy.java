package com.kt.onrace.domain.mypage.service;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.util.Preconditions;

/**
 * 마이페이지 목록 조회에 공통으로 사용하는 페이징 규칙을 모아둔 유틸리티 클래스이다.
 * 기본 페이지 크기, 최대 조회 크기, 파라미터 검증과 오프셋 계산을 담당한다.
 */
final class MyPagePagingPolicy {

	static final int DEFAULT_PAGE = 0;
	static final int DEFAULT_SIZE = 20;
	static final int SUMMARY_SIZE = 3;
	static final int MAX_SIZE = 100;

	private MyPagePagingPolicy() {
	}

	static void validate(int page, int size) {
		Preconditions.validate(page >= 0, BusinessErrorCode.COMMON_INVALID_PARAMETER);
		Preconditions.validate(size > 0 && size <= MAX_SIZE, BusinessErrorCode.COMMON_INVALID_PARAMETER);
	}

	static long offset(int page, int size) {
		return (long) page * size;
	}
}
