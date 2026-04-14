// ========================================================
// k6 부하테스트 로거 (대규모 테스트 최적화)
//
// - errorLog: 모든 VU — 에러/예외 상황만
// - resultLog: VU 1만 — 정상 흐름 검증용
// ========================================================

/**
 * 에러 로그 (모든 VU 출력)
 */
export function errorLog(vu, msg) {
  console.error(`[VU ${vu}] ERROR: ${msg}`);
}

/**
 * 결과 로그 (VU 1만 출력 — 흐름 검증용)
 */
export function resultLog(vu, result, retries) {
  if (vu !== 1) return;
  const retryInfo = retries > 0 ? ` (재시도 ${retries}회)` : '';
  console.log(`[VU 1] ${result}${retryInfo}`);
}
