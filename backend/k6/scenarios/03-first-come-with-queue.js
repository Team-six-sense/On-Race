// ========================================================
// 시나리오 3: 선착순 대기열O 부하 테스트 (2웨이브)
//
// Wave 1: VU 1~VU_COUNT — 대기열 → 신청 → 이탈/결제확정
// Wave 2: VU VU_COUNT+1~총VU — TTL 만료 대기 → 대기열 → 이탈 재고 재선점 → 전원 결제확정
//
// 핵심: 오버셀링 0건 + 이탈자 재고 재순환 검증 (대기열 경유)
// 실행: k6 run -e VU_COUNT=100 k6/scenarios/03-first-come-with-queue.js
// ========================================================

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import { batchLoginAll, authHeaders } from '../lib/auth.js';
import { setupTestData } from '../lib/setup.js';
import { assignPace } from '../lib/distribution.js';
import { withRetry } from '../lib/retry.js';
import { errorLog, resultLog } from '../lib/log.js';
import {
  BASE_URL, VU_COUNT, EXTRA_VU_COUNT,
  RAMP_UP_SEC, HOLD_SEC, RAMP_DOWN_SEC,
  SETUP_TIMEOUT_SEC,
  QUEUE_POLL_INTERVAL_SEC, QUEUE_MAX_POLL_COUNT,
  PAYMENT_DROPOUT_RATIO, RETRY_MAX_ROUNDS, RETRY_WAIT_SEC,
} from '../lib/config.js';

const TOTAL_VUS = VU_COUNT + EXTRA_VU_COUNT;

// 커스텀 메트릭
const queueWaitTime     = new Trend('queue_wait_time');
const queuePassCount    = new Counter('queue_pass_count');
const queueTimeoutCount = new Counter('queue_timeout_count');
const reserveSuccess    = new Counter('queue_reserve_success');
const paymentConfirmed  = new Counter('queue_payment_confirmed');
const paymentDropout    = new Counter('queue_payment_dropout');
const wave2Confirmed    = new Counter('queue_wave2_confirmed');
const soldOut           = new Counter('queue_sold_out');
const alreadyReserved   = new Counter('queue_already_reserved');
const unexpectedErr     = new Counter('queue_unexpected_error');

export const options = {
  setupTimeout: `${SETUP_TIMEOUT_SEC}s`,
  scenarios: {
    queue: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: `${RAMP_UP_SEC}s`, target: TOTAL_VUS },
        { duration: `${HOLD_SEC}s`, target: TOTAL_VUS },
        { duration: `${RAMP_DOWN_SEC}s`, target: 0 },
      ],
    },
  },
  thresholds: {
    'http_req_duration{name:queue_enter}': ['p(95)<3000'],
    'http_req_duration{name:queue_poll}':  ['p(95)<3000'],
    'http_req_duration{name:queue_apply}': ['p(95)<5000'],
    'queue_wait_time': ['p(95)<300000'],
  },
};

export function setup() {
  const testData = setupTestData('FIRST_COME_WITH_QUEUE');
  const tokens = batchLoginAll(TOTAL_VUS);
  console.log(`[setup] 2웨이브 구성 — Wave1: ${VU_COUNT}명, Wave2: ${EXTRA_VU_COUNT}명, 총 ${TOTAL_VUS}명`);
  return { tokens, eventIds: testData.eventIds, paceMap: testData.paceMap };
}

export default function (data) {
  if (__ITER > 0) {
    sleep(HOLD_SEC);
    return;
  }

  let totalRetries = 0;

  const token = data.tokens[String(__VU)];
  if (!token) {
    errorLog(__VU, '토큰 미획득, 건너뜀');
    return;
  }

  const isWave2 = __VU > VU_COUNT;
  const eventId = data.eventIds.FIRST_COME_WITH_QUEUE;
  const { courseId, paceId } = assignPace(__VU, data.paceMap[String(eventId)]);
  const headers = authHeaders(token);

  // Wave 2: TTL 만료 대기 후 시작
  if (isWave2) {
    sleep(RETRY_WAIT_SEC);
  }

  // 재고 조회
  const stockRes = http.get(
    `${BASE_URL}/main/events/${eventId}/entries/stock-check?paceId=${paceId}`,
    { headers, tags: { name: 'stock_check' } }
  );
  check(stockRes, {
    'stock-check 200': (r) => r.status === 200,
  });

  // 토큰 검증 (Wave 1만 — Wave 2는 스킵하여 시간 절약)
  if (!isWave2) {
    const noAuthEnterRes = http.post(
      `${BASE_URL}/queue/enter`,
      JSON.stringify({ paceId }),
      {
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer invalid-token' },
        tags: { name: 'neg_no_auth_queue' },
        responseCallback: http.expectedStatuses(401),
      }
    );
    check(noAuthEnterRes, { 'invalid JWT → queue 401': (r) => r.status === 401 });
  }

  // 대기열 진입 (최대 3회 재시도)
  const { res: enterRes, retries: enterRetries } = withRetry(
    () => http.post(
      `${BASE_URL}/queue/enter`,
      JSON.stringify({ paceId }),
      { headers, tags: { name: 'queue_enter' } }
    ),
    { maxRetries: 3, backoffSec: 2, name: '대기열 진입', vu: __VU }
  );
  totalRetries += enterRetries;

  check(enterRes, { 'queue enter 200': (r) => r.status === 200 });
  if (enterRes.status !== 200) {
    errorLog(__VU, `대기열 진입 실패 (${enterRes.status})`);
    return;
  }

  // 대기열 폴링 (PASS까지)
  const startWait = Date.now();
  let passed = false;
  let passToken = null;
  let pollCount = 0;

  for (let i = 0; i < QUEUE_MAX_POLL_COUNT; i++) {
    sleep(QUEUE_POLL_INTERVAL_SEC);
    pollCount++;

    const statusRes = http.get(
      `${BASE_URL}/queue/status?paceId=${paceId}`,
      { headers, tags: { name: 'queue_poll' } }
    );

    if (statusRes.status === 200) {
      try {
        const body = statusRes.json();
        if (body.data && body.data.status === 'PASS') {
          passed = true;
          passToken = body.data.passToken;
          queueWaitTime.add(Date.now() - startWait);
          queuePassCount.add(1);
          break;
        }
      } catch (e) { /* 다음 폴링 계속 */ }
    }
  }

  if (!passed) {
    queueTimeoutCount.add(1);
    errorLog(__VU, `대기열 타임아웃 (${pollCount}회 폴링)`);
    return;
  }

  // 대기열 토큰 검증 (Wave 1만)
  if (!isWave2) {
    const noQueueTokenRes = http.post(
      `${BASE_URL}/main/events/${eventId}/entries/apply/first-come`,
      JSON.stringify({ courseId, paceId }),
      {
        headers,
        tags: { name: 'neg_no_queue_token' },
        responseCallback: http.expectedStatuses(429),
      }
    );
    check(noQueueTokenRes, { 'no queue token → 429': (r) => r.status === 429 });
  }

  // ── 선착순 신청 + 결제 루프 ──
  const applyHeaders = Object.assign({}, headers, { 'X-Queue-Token': passToken });

  for (let round = 0; round <= RETRY_MAX_ROUNDS; round++) {
    if (round > 0) sleep(RETRY_WAIT_SEC);

    const { res: applyRes, retries: applyRetries } = withRetry(
      () => http.post(
        `${BASE_URL}/main/events/${eventId}/entries/apply/first-come`,
        JSON.stringify({ courseId, paceId }),
        {
          headers: applyHeaders,
          tags: { name: 'queue_apply' },
          responseCallback: http.expectedStatuses(200, 400, 409, 429),
        }
      ),
      { maxRetries: 1, backoffSec: 2, name: '선착순 신청', vu: __VU }
    );
    totalRetries += applyRetries;

    if (applyRes.status === 429) {
      unexpectedErr.add(1);
      errorLog(__VU, `passToken 만료 (429, 라운드 ${round})`);
      break;
    }

    if (applyRes.status === 200) {
      reserveSuccess.add(1);

      // Wave 1: 이탈/확정 판단 / Wave 2: 전원 결제확정
      if (!isWave2 && Math.random() < PAYMENT_DROPOUT_RATIO) {
        paymentDropout.add(1);
        resultLog(__VU, `결제 이탈 (라운드 ${round})`, totalRetries);
        break;
      }

      const confirmRes = http.post(
        `${BASE_URL}/main/internal/load-test/confirm-reservation`,
        JSON.stringify({ paceId }),
        { headers, tags: { name: 'confirm_reservation' } }
      );

      if (confirmRes.status === 200) {
        if (isWave2) {
          wave2Confirmed.add(1);
          resultLog(__VU, `Wave2 결제 확정 (라운드 ${round})`, totalRetries);
        } else {
          paymentConfirmed.add(1);
          resultLog(__VU, `결제 확정 (라운드 ${round})`, totalRetries);
        }
      } else {
        unexpectedErr.add(1);
        errorLog(__VU, `결제 확정 실패 (${confirmRes.status}, 라운드 ${round})`);
      }
      break;

    } else if (applyRes.status === 409) {
      try {
        const code = applyRes.json().code;
        if (code === 'ENT_010') {
          alreadyReserved.add(1);
        }
        if (round === RETRY_MAX_ROUNDS) {
          soldOut.add(1);
          resultLog(__VU, `최종 매진 (${code})`, totalRetries);
        }
      } catch (e) {
        if (round === RETRY_MAX_ROUNDS) {
          soldOut.add(1);
          resultLog(__VU, '최종 매진 (409)', totalRetries);
        }
      }

    } else {
      unexpectedErr.add(1);
      errorLog(__VU, `예상 외 에러 (${applyRes.status}, 라운드 ${round})`);
      break;
    }
  }
}
