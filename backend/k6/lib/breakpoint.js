// ========================================================
// 용량(Breakpoint) 테스트 헬퍼 라이브러리
//
// - 계단형 stages 자동 생성
// - VU별 유저 토큰 순환 (30,000명 풀 활용)
// ========================================================

// ================================================================
// 계단형 stages 생성
// ================================================================

/**
 * ramping-vus용 계단형(Staircase) stages 배열 생성
 *
 * @param {number} startVUs   - 시작 VU 수
 * @param {number} stepVUs    - 단계별 VU 증분
 * @param {number} maxVUs     - 최대 VU 수
 * @param {number} durationSec - 단계 유지 시간 (초)
 * @param {number} rampSec    - 단계 간 ramp 시간 (초)
 * @returns {Array<{duration: string, target: number}>}
 *
 * 예: start=500, step=500, max=2000, duration=60, ramp=10
 * → [ {10s, 500}, {60s, 500}, {10s, 1000}, {60s, 1000}, {10s, 1500}, {60s, 1500}, {10s, 2000}, {60s, 2000} ]
 */
export function generateStaircaseStages(startVUs, stepVUs, maxVUs, durationSec, rampSec) {
  const stages = [];

  for (let vus = startVUs; vus <= maxVUs; vus += stepVUs) {
    stages.push({ duration: `${rampSec}s`, target: vus });
    stages.push({ duration: `${durationSec}s`, target: vus });
  }

  return stages;
}

/**
 * stages 구성을 사람이 읽을 수 있는 문자열로 변환
 * 예: "500 → 1000 → 1500 → 2000 VUs"
 */
export function describeStages(startVUs, stepVUs, maxVUs) {
  const steps = [];
  for (let vus = startVUs; vus <= maxVUs; vus += stepVUs) {
    steps.push(vus);
  }
  return steps.join(' → ') + ' VUs';
}
