// ========================================================
// 유저 → 페이스 배분 로직
// 70% HOT 페이스 집중, 30% 나머지 페이스 분산
//
// setup API 응답의 paceMap을 사용하여 동적 배분
// paceMap[eventId] = { hot: [{ courseId, paceId, stock }], others: [...] }
// ========================================================

import { HOT_PACE_RATIO, VU_COUNT } from './config.js';

/**
 * VU를 페이스에 배분 (동적 paceMap 기반)
 * @param {number} vuIndex - VU 번호 (1-based, __VU)
 * @param {Object} paceMapEntry - { hot: [{ courseId, paceId }], others: [{ courseId, paceId }, ...] }
 * @returns {{ courseId: number, paceId: number }}
 */
export function assignPace(vuIndex, paceMapEntry) {
  if (!paceMapEntry || !paceMapEntry.hot || paceMapEntry.hot.length === 0) {
    console.error(`VU ${vuIndex}: paceMapEntry가 없습니다`);
    return { courseId: 0, paceId: 0 };
  }

  const hotCutoff = Math.floor(VU_COUNT * HOT_PACE_RATIO);

  // 70%: HOT 페이스 (여러 개일 경우 라운드로빈)
  if (vuIndex <= hotCutoff) {
    const hotPaces = paceMapEntry.hot;
    const idx = (vuIndex - 1) % hotPaces.length;
    return { courseId: hotPaces[idx].courseId, paceId: hotPaces[idx].paceId };
  }

  // 30%: 나머지 페이스 라운드로빈
  const others = paceMapEntry.others;
  if (!others || others.length === 0) {
    const fallback = paceMapEntry.hot[0];
    return { courseId: fallback.courseId, paceId: fallback.paceId };
  }

  const idx = (vuIndex - hotCutoff - 1) % others.length;
  return { courseId: others[idx].courseId, paceId: others[idx].paceId };
}
