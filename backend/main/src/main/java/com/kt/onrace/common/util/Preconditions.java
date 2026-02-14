package com.kt.onrace.common.util;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;

public class Preconditions {

	// Business 관련 에러만 처리해야합니다, 내부용 에러 exception는 처리하지 마세요!
	public static void validate(boolean expression, BusinessErrorCode errorCode) {
		if (!expression) {
			throw new BusinessException(errorCode);
		}
	}
}
