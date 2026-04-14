// ========================================================
// HTTP 요청 재시도 헬퍼
// 5xx 서버 에러 시 지수 백오프로 재시도
// ========================================================

import { sleep } from 'k6';
import { Counter } from 'k6/metrics';

export const retryCount   = new Counter('retry_count');
export const retrySuccess = new Counter('retry_success');

/**
 * HTTP 요청 함수를 재시도하는 래퍼
 *
 * @param {Function} requestFn - () => http.Response 반환 함수
 * @param {Object} [opts]
 * @param {number} [opts.maxRetries=3]  - 최대 재시도 횟수
 * @param {number} [opts.backoffSec=2]  - 초기 백오프(초)
 * @param {string} [opts.name='request'] - 로그용 이름
 * @param {number} [opts.vu=0]          - VU 번호 (로그용)
 * @returns {{ res: Object, retries: number }}
 */
export function withRetry(requestFn, opts = {}) {
  const maxRetries = opts.maxRetries != null ? opts.maxRetries : 3;
  const backoffSec = opts.backoffSec != null ? opts.backoffSec : 2;
  const name = opts.name || 'request';
  const vu = opts.vu || 0;

  let res = requestFn();
  let retries = 0;

  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    // 5xx만 재시도, 4xx(비즈니스 에러)는 즉시 반환
    if (res.status < 500) {
      break;
    }

    retryCount.add(1);
    retries = attempt;

    const delaySec = backoffSec * Math.pow(2, attempt - 1) * (0.5 + Math.random() * 0.5);
    console.warn(`[VU ${vu}] ${name} 재시도 (${attempt}/${maxRetries}) — ${res.status}, ${delaySec.toFixed(1)}초 후 재시도`);

    sleep(delaySec);
    res = requestFn();
  }

  if (retries > 0 && res.status < 500) {
    retrySuccess.add(1);
  }

  return { res, retries };
}
