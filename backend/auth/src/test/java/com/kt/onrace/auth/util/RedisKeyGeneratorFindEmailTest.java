package com.kt.onrace.auth.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kt.onrace.common.util.RedisKeyGenerator;

class RedisKeyGeneratorFindEmailTest {

	@Test
	@DisplayName("smsFindIpAttemptKey: 'sms:find:ip_attempt:{ip}' 형식 반환")
	void smsFindIpAttemptKey_returnsExpectedFormat() {
		assertThat(RedisKeyGenerator.smsFindIpAttemptKey("203.0.113.1"))
			.isEqualTo("sms:find:ip_attempt:203.0.113.1");
	}
}
