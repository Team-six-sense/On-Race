// ========================================================
// 부하테스트 데이터 자동 셋업 헬퍼
// setup()에서 호출하여 DB/Redis 자동 초기화
//
// 반환값:
//   eventIds  — { LOTTERY: N } | { FIRST_COME_NO_QUEUE: N } | ... (scenarioType에 따라 1개 또는 전체)
//   paceMap   — { "N": { hot: [{ courseId, paceId, stock }], others: [...] }, ... }
// ========================================================

import http from 'k6/http';
import { check } from 'k6';
import {
  BASE_URL, VU_COUNT, TOTAL_STOCK,
  COMPETITION_RATIO, HOT_PACE_COUNT, HOT_STOCK_RATIO,
  HOT_COURSE_INDEX, HOT_PACE_INDEX, PRE_SAVE_RATIO,
} from './config.js';

/**
 * 부하테스트 데이터 자동 셋업
 * @param {string|null} scenarioType — 'LOTTERY' | 'FIRST_COME_NO_QUEUE' | 'FIRST_COME_WITH_QUEUE' | null(전체)
 * @returns {{ eventIds: Object, paceMap: Object }}
 */
export function setupTestData(scenarioType = null) {
  const estimatedStock = TOTAL_STOCK || Math.ceil(VU_COUNT / COMPETITION_RATIO);
  console.log(`[setup] 테스트 데이터 셋업 시작 (scenario=${scenarioType || 'ALL'}, totalUsers=${VU_COUNT}, totalStock=${TOTAL_STOCK ? TOTAL_STOCK + '(직접지정)' : estimatedStock + '(자동계산)'}, hotPaces=${HOT_PACE_COUNT}, hotRatio=${HOT_STOCK_RATIO})`);

  const body = {
    totalUsers: VU_COUNT,
    competitionRatio: COMPETITION_RATIO,
    hotPaceCount: HOT_PACE_COUNT,
    hotStockRatio: HOT_STOCK_RATIO,
  };
  if (scenarioType) body.scenarioType = scenarioType;
  if (TOTAL_STOCK) body.totalStock = TOTAL_STOCK;
  if (HOT_COURSE_INDEX != null) body.hotCourseIndex = HOT_COURSE_INDEX;
  if (HOT_PACE_INDEX != null) body.hotPaceIndex = HOT_PACE_INDEX;
  if (PRE_SAVE_RATIO != null) body.preSaveRatio = PRE_SAVE_RATIO;

  const res = http.post(
    `${BASE_URL}/main/internal/load-test/setup`,
    JSON.stringify(body),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'setup_test_data' },
      timeout: '300s',
    }
  );

  const ok = check(res, {
    '테스트 데이터 셋업 성공': (r) => r.status === 200,
  });

  if (!ok) {
    console.error(`[setup] 테스트 데이터 셋업 실패: ${res.status} ${res.body}`);
    throw new Error('테스트 데이터 셋업 실패 — 서버 상태를 확인하세요');
  }

  const data = res.json().data;
  console.log(`[setup] 셋업 완료 — users=${data.userCount} (skipped=${data.userSkipped}), events=${JSON.stringify(data.eventIds)}, totalStock=${data.totalStock}, preSave=${data.preSaveCount || 0}`);

  // 인기 페이스 정보 로그
  for (const [eventId, entry] of Object.entries(data.paceMap)) {
    const hotInfo = entry.hot.map((p) => `paceId=${p.paceId}(${p.stock}석)`).join(', ');
    console.log(`[setup] 이벤트 ${eventId} 인기 페이스: ${hotInfo}`);
  }

  return {
    eventIds: data.eventIds,
    paceMap: data.paceMap,
  };
}
