package com.kt.onrace.domain.order.service;

import java.time.Duration;

/**
 * prepare token nonce를 원자적으로 사용 처리해 1회성 토큰처럼 동작하게 만든다.
 */
public interface OrderPrepareTokenNonceStore {

	boolean markAsUsed(String nonce, Duration ttl);
}
