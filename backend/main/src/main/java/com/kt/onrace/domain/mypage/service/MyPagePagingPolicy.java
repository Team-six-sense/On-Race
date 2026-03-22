package com.kt.onrace.domain.mypage.service;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.util.Preconditions;

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
