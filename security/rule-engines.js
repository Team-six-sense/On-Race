/**
 * rule-engine.js
 * 룰 베이스 Bot Detection - 백엔드 판정 엔진
 */
const { checkIP } = require('./cti-checker'); // 추가
const {
  THRESHOLDS,
  CRITICAL_AUTOMATION_GROUP,
  CRITICAL_SINGLE_RULES,
  SWIFT_SHADER_COMBOS,
  RULES,
} = require('./rules-config');

/**
 * 메인 판정 함수 (Async로 변경)
 * @param {Object} signals - 수집된 신호
 * @param {string} ip - 사용자 IP 주소
 */
async function evaluate(signals, ip) {
  const triggeredRules = [];

  // ── 0단계: CTI 체크 (가장 먼저 실행) ──
  const cti = await checkIP(ip);
  if (cti.isMalicious) {
    return buildResult('BLOCK', 100, ['cti_abuseipdb']);
  }

  // ─────────────────────────────────────────────
  // 1단계: Critical 룰 체크 (즉시 BLOCK)
  // ─────────────────────────────────────────────
  const automationHit = CRITICAL_AUTOMATION_GROUP.find(ruleId => signals[ruleId]);
  if (automationHit) {
    CRITICAL_AUTOMATION_GROUP.forEach(id => {
      if (signals[id]) triggeredRules.push(id);
    });
    return buildResult('BLOCK', 100, triggeredRules);
  }

  for (const ruleId of CRITICAL_SINGLE_RULES) {
    if (signals[ruleId]) {
      triggeredRules.push(ruleId);
      return buildResult('BLOCK', 100, triggeredRules);
    }
  }

  // ─────────────────────────────────────────────
  // 2단계: 조합 룰 (SwiftShader)
  // ─────────────────────────────────────────────
  if (signals.swiftShader) {
    triggeredRules.push('swiftShader');
    let compositeScore = SWIFT_SHADER_COMBOS.baseScore;
    const bonuses = SWIFT_SHADER_COMBOS.bonusScores;

    if (signals.noMouse)     { compositeScore += bonuses.noMouse;     triggeredRules.push('noMouse_combo'); }
    if (signals.uaStructure) { compositeScore += bonuses.uaStructure; triggeredRules.push('uaStructure_combo'); }
    if (signals.noFocusBlur) { compositeScore += bonuses.noFocusBlur; triggeredRules.push('noFocusBlur_combo'); }

    if (compositeScore >= SWIFT_SHADER_COMBOS.blockThreshold) {
      return buildResult('BLOCK', compositeScore, triggeredRules);
    }
  }

  // ─────────────────────────────────────────────
  // 3단계: 일반 룰 누적 점수 계산
  // ─────────────────────────────────────────────
  const alreadyTracked = new Set(triggeredRules);
  let totalScore = 0;

  for (const rule of RULES) {
    if (signals[rule.id]) {
      totalScore += rule.score;
      if (!alreadyTracked.has(rule.id)) {
        triggeredRules.push(rule.id);
      }
    }
  }

  // 최종 판정
  if (totalScore >= THRESHOLDS.BLOCK)     return buildResult('BLOCK',     totalScore, triggeredRules);
  if (totalScore >= THRESHOLDS.CHALLENGE) return buildResult('CHALLENGE', totalScore, triggeredRules);
  return buildResult('ALLOW', totalScore, triggeredRules);
}

function buildResult(action, score, triggeredRules) {
  return { action, score, triggeredRules };
}

module.exports = { evaluate };