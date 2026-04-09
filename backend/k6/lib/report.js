// ========================================================
// 공유 리포트 템플릿 라이브러리
//
// 부하 테스트(01~03) + 용량 테스트(04) 공용
// 성능 지표 + 비즈니스 지표 통합 리포트 생성
// ========================================================

import { textSummary } from 'https://jslib.k6.io/k6-summary/0.1.0/index.js';
import { describeStages } from './breakpoint.js';

// ================================================================
// 공통 유틸
// ================================================================

function fmt(n) {
  return n != null ? n.toLocaleString() : '0';
}

function pct(part, total) {
  if (!total || total === 0) return '0.0';
  return ((part / total) * 100).toFixed(1);
}

function counter(metrics, name) {
  return metrics[name] ? metrics[name].values.count : 0;
}

function formatDuration(ms) {
  const sec = ms / 1000;
  const mins = Math.floor(sec / 60);
  const secs = Math.round(sec % 60);
  return `${mins}분 ${secs}초`;
}

// ================================================================
// 헤더 섹션
// ================================================================

function formatLoadTestHeader(config) {
  const lines = [
    '',
    '========================================',
    '         부하 테스트 리포트',
    '========================================',
    `테스트 흐름:   ${config.flow}`,
    `동시 사용자:   ${fmt(config.vuCount)} VUs`,
  ];
  if (config.totalStock != null) {
    lines.push(`총 재고:       ${fmt(config.totalStock)}석`);
  }
  return lines;
}

function formatBreakpointHeader(config) {
  const stagesDesc = describeStages(config.startVUs, config.stepVUs, config.maxVUs);
  return [
    '',
    '========================================',
    '     용량(Breakpoint) 테스트 리포트',
    '========================================',
    `테스트 흐름:   ${config.flow}`,
    `스테이지 구성:  ${stagesDesc}`,
    `단계 유지:     ${config.stepDurationSec}초 (ramp ${config.rampSec}초)`,
    `로그인 풀:     ${config.loginPool ? fmt(config.loginPool) + '명' : 'BP_MAX_VUS와 동일'}`,
    `중단 임계값:   에러율 > ${(config.errorThreshold * 100).toFixed(0)}% 또는 p95 > ${config.p95ThresholdMs}ms`,
  ];
}

// ================================================================
// 성능 지표 섹션
// ================================================================

function formatPerformanceMetrics(data) {
  const metrics = data.metrics;
  const durationMs = data.state.testRunDurationMs;
  const durationSec = durationMs / 1000;

  const totalReqs = metrics.http_reqs ? metrics.http_reqs.values.count : 0;
  const avgRPS = totalReqs > 0 ? (totalReqs / durationSec).toFixed(1) : '0';
  const maxVUs = metrics.vus_max ? metrics.vus_max.values.value : 0;

  // 에러율: error_rate (커스텀 Rate) 또는 http_req_failed (내장)
  // k6 Rate: add(true)=passes(에러), add(false)=fails(성공)
  let errorRate, errorCount;
  if (metrics.error_rate) {
    errorRate = (metrics.error_rate.values.rate * 100).toFixed(2);
    errorCount = metrics.error_rate.values.passes || 0;
  } else {
    const failed = metrics.http_req_failed ? metrics.http_req_failed.values.rate : 0;
    errorRate = (failed * 100).toFixed(2);
    errorCount = metrics.http_req_failed ? metrics.http_req_failed.values.passes || 0 : 0;
  }

  // 응답시간: apply_latency (커스텀 Trend) 또는 http_req_duration (내장)
  const latencyMetric = metrics.apply_latency || metrics.http_req_duration;
  const latency = latencyMetric ? latencyMetric.values : {};
  const p50 = latency.med != null ? Math.round(latency.med) : '-';
  const p90 = latency['p(90)'] != null ? Math.round(latency['p(90)']) : '-';
  const p95 = latency['p(95)'] != null ? Math.round(latency['p(95)']) : '-';

  return {
    lines: [
      `테스트 시간:   ${formatDuration(durationMs)}`,
      '',
      '성능 지표:',
      `  총 요청:     ${fmt(totalReqs)}`,
      `  평균 RPS:    ${avgRPS}`,
      `  에러율:      ${errorRate}% (${fmt(errorCount)}건)`,
      `  p50: ${p50}ms  |  p90: ${p90}ms  |  p95: ${p95}ms`,
    ],
    // 용량 테스트 결과 판정용 내부 데이터
    totalReqs,
    avgRPS,
    maxVUs,
    durationSec,
    errorCount,
  };
}

// ================================================================
// 비즈니스 지표 섹션
// ================================================================

function formatBusinessMetrics(data, flow) {
  const m = data.metrics;
  const lines = ['', '비즈니스 지표:'];

  const applyOk        = counter(m, 'apply_ok');
  const applyDup       = counter(m, 'apply_dup');
  const confirmOk      = counter(m, 'confirm_ok');
  const paymentDropout = counter(m, 'payment_dropout');
  const wave2Ok        = counter(m, 'wave2_ok');
  const soldOutCount   = counter(m, 'sold_out');
  const blockedCount   = counter(m, 'blocked');
  const unexpectedErr  = counter(m, 'unexpected_error');
  const queuePassCount = counter(m, 'queue_pass');
  const queueTimeoutCt = counter(m, 'queue_timeout');

  const totalApply = applyOk + applyDup;

  // 대기열 지표 (FIRST_COME_QUEUE만)
  if (flow === 'FIRST_COME_QUEUE') {
    lines.push(`  대기열 통과:    ${fmt(queuePassCount)}건`);
    lines.push(`  대기열 타임아웃: ${fmt(queueTimeoutCt)}건`);

    // 대기열 대기시간 (Trend)
    if (m.queue_wait_time) {
      const wt = m.queue_wait_time.values;
      const wtMed = wt.med != null ? Math.round(wt.med) : '-';
      const wt95 = wt['p(95)'] != null ? Math.round(wt['p(95)']) : '-';
      lines.push(`  대기 시간:      p50=${wtMed}ms  |  p95=${wt95}ms`);
    }
  }

  // 신청 지표 (flow별 분기)
  if (flow === 'LOTTERY') {
    lines.push(`  응모 성공:      ${fmt(applyOk)}건${totalApply > 0 ? ` (${pct(applyOk, totalApply)}%)` : ''}`);
  } else {
    lines.push(`  선점 성공:      ${fmt(applyOk)}건${totalApply > 0 ? ` (${pct(applyOk, totalApply)}%)` : ''}`);
  }
  lines.push(`  중복/매진:      ${fmt(applyDup)}건${totalApply > 0 ? ` (${pct(applyDup, totalApply)}%)` : ''}`);

  // 결제 지표 (FIRST_COME / FIRST_COME_QUEUE)
  if (flow === 'FIRST_COME' || flow === 'FIRST_COME_QUEUE') {
    const totalConfirm = confirmOk + wave2Ok;
    lines.push(`  Wave1 결제 확정: ${fmt(confirmOk)}건${applyOk > 0 ? ` (확정률 ${pct(confirmOk, applyOk)}%)` : ''}`);
    lines.push(`  Wave2 결제 확정: ${fmt(wave2Ok)}건`);
    lines.push(`  총 결제 확정:   ${fmt(totalConfirm)}건`);
    lines.push(`  결제 이탈:      ${fmt(paymentDropout)}건`);
    lines.push(`  재선점:         ${fmt(wave2Ok)}건${paymentDropout > 0 ? ` (재선점률 ${pct(wave2Ok, paymentDropout)}%)` : ''}`);
    lines.push(`  매진 차단:      ${fmt(soldOutCount)}건`);
  }

  // 비즈니스 차단 + 서버 에러 (공통)
  lines.push(`  비즈니스 차단:  ${fmt(blockedCount)}건`);
  lines.push(`  서버 에러:      ${fmt(unexpectedErr)}건`);

  return lines;
}

// ================================================================
// 용량 테스트 결과 판정 섹션
// ================================================================

function formatBreakpointResult(data, config, perf) {
  const perStepSec = config.rampSec + config.stepDurationSec;
  const reachedStep = Math.floor(perf.durationSec / perStepSec) + 1;
  const totalSteps = Math.ceil((config.maxVUs - config.startVUs) / config.stepVUs) + 1;
  const reachedVUs = Math.min(
    config.startVUs + (reachedStep - 1) * config.stepVUs,
    config.maxVUs
  );

  const aborted = reachedStep < totalSteps;
  const stableStep = aborted ? Math.max(1, reachedStep - 1) : reachedStep;
  const stableVUs = Math.min(
    config.startVUs + (stableStep - 1) * config.stepVUs,
    config.maxVUs
  );

  const lines = [];

  if (aborted) {
    lines.push('========================================');
    lines.push(' 한계점 도달 — 임계값 초과로 자동 중단');
    lines.push('----------------------------------------');
    lines.push(` 도달 단계:     ${reachedStep}단계 (${reachedVUs} VUs)`);
    lines.push(` 최대 안정 단계: ${stableStep}단계 (${stableVUs} VUs)`);
    lines.push(` 안정 RPS:      ~${perf.avgRPS} req/s`);
    lines.push('========================================');
    lines.push(` 권장 스케일링 기준값:`);
    lines.push(`  - Pod당 안정 VU:  ~${stableVUs}`);
    lines.push(`  - 안전 마진 70%:  ~${Math.round(stableVUs * 0.7)}`);
    lines.push('========================================');
  } else {
    lines.push('========================================');
    lines.push(' 모든 단계 통과 — 한계점 미도달');
    lines.push('----------------------------------------');
    lines.push(` 최종 단계: ${totalSteps}단계 (${config.maxVUs} VUs)`);
    lines.push(` 안정 RPS: ~${perf.avgRPS} req/s`);
    lines.push('========================================');
    lines.push(' BP_MAX_VUS를 늘려 재시도하세요.');
    lines.push('========================================');
  }

  return lines;
}

// ================================================================
// 최종 리포트 조합
// ================================================================

/**
 * handleSummary에서 호출하여 최종 출력 객체 생성
 *
 * @param {Object} data   - k6 handleSummary에 전달되는 data 객체
 * @param {Object} config - 테스트 설정 정보
 *   config.testType:  'LOAD' | 'BREAKPOINT'
 *   config.flow:      'LOTTERY' | 'FIRST_COME' | 'FIRST_COME_QUEUE'
 *   // LOAD 전용
 *   config.vuCount, config.totalStock
 *   // BREAKPOINT 전용
 *   config.startVUs, config.stepVUs, config.maxVUs, config.stepDurationSec,
 *   config.rampSec, config.errorThreshold, config.p95ThresholdMs, config.loginPool
 */
export function buildSummaryOutput(data, config) {
  // 1. 헤더
  const header = config.testType === 'BREAKPOINT'
    ? formatBreakpointHeader(config)
    : formatLoadTestHeader(config);

  // 2. 성능 지표
  const perf = formatPerformanceMetrics(data);

  // 3. 비즈니스 지표
  const business = formatBusinessMetrics(data, config.flow);

  // 4. 용량 테스트 결과 (BREAKPOINT만)
  const bpResult = config.testType === 'BREAKPOINT'
    ? formatBreakpointResult(data, config, perf)
    : ['========================================'];

  // 조합
  const report = [
    ...header,
    ...perf.lines,
    ...business,
    '',
    ...bpResult,
    '',
  ].join('\n');

  const defaultSummary = textSummary(data, { indent: '  ', enableColors: true });

  return {
    stdout: defaultSummary + '\n' + report,
  };
}
