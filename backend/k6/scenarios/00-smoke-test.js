// ========================================================
// 스모크 테스트: 환경 검증 (5 VU)
//
// 목적: 본 테스트 전 서비스 정상 동작 확인
// 흐름: 데이터 셋업 → 로그인 → 이벤트 조회 (3개) → 재고 확인
// 실행: k6 run k6/scenarios/00-smoke-test.js
// ========================================================

import http from 'k6/http';
import { check, sleep } from 'k6';
import { batchLoginAll, authHeaders } from '../lib/auth.js';
import { setupTestData } from '../lib/setup.js';
import { BASE_URL } from '../lib/config.js';

const SMOKE_VU = 5;

export const options = {
  vus: SMOKE_VU,
  iterations: SMOKE_VU,
  setupTimeout: '300s',
  thresholds: {
    http_req_failed: ['rate==0'],
  },
};

// ── setup: 데이터 셋업 → 5명 로그인 ──
export function setup() {
  const testData = setupTestData();
  const tokens = batchLoginAll(SMOKE_VU);
  return { tokens, eventIds: testData.eventIds, paceMap: testData.paceMap };
}

export default function (data) {
  // 1. 사전 로그인 토큰 획득
  const token = data.tokens[String(__VU)];
  const loginOk = check(token, {
    '로그인 성공': (t) => t != null,
  });
  if (!loginOk) return;

  const headers = authHeaders(token);

  // 2. 각 이벤트 조회 (동적 ID)
  const eventIds = [
    data.eventIds.LOTTERY,
    data.eventIds.FIRST_COME_NO_QUEUE,
    data.eventIds.FIRST_COME_WITH_QUEUE,
  ];
  for (const eid of eventIds) {
    const res = http.get(`${BASE_URL}/main/events/${eid}`, {
      headers: headers,
      tags: { name: 'event_detail' },
    });
    check(res, {
      [`이벤트 ${eid} 조회 성공`]: (r) => r.status === 200,
    });
  }

  // 3. 재고 확인 (paceMap에서 HOT paceId 동적 추출)
  const fcNoQueueId = data.eventIds.FIRST_COME_NO_QUEUE;
  const paceMapEntry = data.paceMap[String(fcNoQueueId)];
  const hotPaceId = paceMapEntry && paceMapEntry.hot ? paceMapEntry.hot.paceId : null;

  if (hotPaceId) {
    const stockRes = http.get(
      `${BASE_URL}/main/events/${fcNoQueueId}/entries/stock-check?paceId=${hotPaceId}`,
      { headers: headers, tags: { name: 'stock_check' } }
    );
    check(stockRes, {
      '재고 조회 성공': (r) => r.status === 200,
    });
  }

  sleep(1);
}
