package com.kt.onrace.common.util;

import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.BusinessErrorCode;

public class Preconditions {
	public static void validate(boolean expression, BusinessErrorCode errorCode) {
		if (!expression) {
			throw new BusinessException(errorCode);
		}
	}
}
