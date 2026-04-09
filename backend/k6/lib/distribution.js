// ========================================================
// 유저 → 페이스 배분 로직
// 70% HOT 페이스 집중, 30% 나머지 페이스 분산
//
// 10-VU 블록 단위 인터리브 배분:
//   매 10명 중 앞 7명 → HOT, 뒤 3명 → 나머지
//   Wave 2 VU(VU_COUNT+1~)도 동일 70/30 비율 보장
//
// setup API 응답의 paceMap을 사용하여 동적 배분
// paceMap[eventId] = { hot: [{ courseId, paceId, stock }], others: [...] }
// ========================================================

import { HOT_PACE_RATIO } from './config.js';

/**
 * VU를 페이스에 배분 (동적 paceMap 기반, 인터리브 70/30)
 *
 * 기존 문제: VU 번호 범위 기반 배분(1~hotCutoff → HOT) 시
 *   Wave 2 VU(VU_COUNT+1~)가 전원 Others로 배분되어
 *   HOT 페이스 이탈 재고를 Wave 2가 회수하지 못함
 *
 * 해결: 10-VU 블록 단위 인터리브 배분으로
 *   VU 번호와 무관하게 일관된 70/30 비율 보장
 *
 * @param {number} vuIndex - VU 번호 (1-based, __VU)
 * @param {Object} paceMapEntry - { hot: [{ courseId, paceId }], others: [{ courseId, paceId }, ...] }
 * @returns {{ courseId: number, paceId: number }}
 */
export function assignPace(vuIndex, paceMapEntry) {
  if (!paceMapEntry || !paceMapEntry.hot || paceMapEntry.hot.length === 0) {
    console.error(`VU ${vuIndex}: paceMapEntry가 없습니다`);
    return { courseId: 0, paceId: 0 };
  }

  // 10-VU 블록 단위 인터리브 배분
  // 예: HOT_PACE_RATIO=0.7 → 매 10명 중 앞 7명 HOT, 뒤 3명 Others
  const RATIO_BASE = 10;
  const hotSlots = Math.round(HOT_PACE_RATIO * RATIO_BASE);
  const posInBlock = (vuIndex - 1) % RATIO_BASE;
  const blockNum = Math.floor((vuIndex - 1) / RATIO_BASE);

  // 블록 내 앞 hotSlots명 → HOT 페이스
  if (posInBlock < hotSlots) {
    const hotPaces = paceMapEntry.hot;
    const hotSeq = blockNum * hotSlots + posInBlock;
    const idx = hotSeq % hotPaces.length;
    return { courseId: hotPaces[idx].courseId, paceId: hotPaces[idx].paceId };
  }

  // 블록 내 나머지 → Others 페이스 라운드로빈
  const others = paceMapEntry.others;
  if (!others || others.length === 0) {
    const fallback = paceMapEntry.hot[0];
    return { courseId: fallback.courseId, paceId: fallback.paceId };
  }

  const otherSeq = blockNum * (RATIO_BASE - hotSlots) + (posInBlock - hotSlots);
  const idx = otherSeq % others.length;
  return { courseId: others[idx].courseId, paceId: others[idx].paceId };
}
