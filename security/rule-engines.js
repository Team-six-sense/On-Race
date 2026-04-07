/**
 * rule-engine.js
 * 룰 베이스 Bot Detection - 백엔드 판정 엔진
 * Canvas/WebGL Fingerprint 탐지 로직 추가
 */
const { checkIP } = require('./cti-checker');
const {
  THRESHOLDS,
  CRITICAL_AUTOMATION_GROUP,
  CRITICAL_SINGLE_RULES,
  SWIFT_SHADER_COMBOS,
  FINGERPRINT_RULES,
  RULES,
} = require('./rules-config');

/**
 * 메인 판정 함수
 * @param {Object} signals - 수집된 신호
 * @param {string} ip - 사용자 IP 주소
 * @param {Object} fingerprint - Canvas/WebGL Fingerprint 데이터 (optional)
 */
async function evaluate(signals, ip, fingerprint = null) {
  const triggeredRules = [];
  let totalScore = 0;

  // ─────────────────────────────────────────────
  // 0단계: CTI 체크 (가장 먼저 실행)
  // ─────────────────────────────────────────────
  try {
    const cti = await checkIP(ip);
    if (cti.isMalicious) {
      return buildResult('BLOCK', 100, ['cti_abuseipdb'], { ctiInfo: cti });
    }
  } catch (error) {
    console.error('[CTI Check Error]', error.message);
    // CTI 실패 시에도 진행 (가용성 우선)
  }

  // ─────────────────────────────────────────────
  // 1단계: Fingerprint 기반 봇 탐지 (Critical)
  // ─────────────────────────────────────────────
  if (fingerprint) {
    const fpResult = checkFingerprint(fingerprint);
    
    // 자동화 도구 직접 탐지 시 즉시 BLOCK
    if (fpResult.isCritical) {
      triggeredRules.push(...fpResult.triggeredRules);
      return buildResult('BLOCK', 100, triggeredRules, { fingerprintReasons: fpResult.reasons });
    }
    
    // 일반 의심 점수 누적
    totalScore += fpResult.score;
    triggeredRules.push(...fpResult.triggeredRules);
  }

  // ─────────────────────────────────────────────
  // 2단계: Critical 룰 체크 (즉시 BLOCK)
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
  // 3단계: 조합 룰 (SwiftShader)
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
    
    totalScore += compositeScore;
  }

  // ─────────────────────────────────────────────
  // 4단계: 일반 룰 누적 점수 계산
  // ─────────────────────────────────────────────
  const alreadyTracked = new Set(triggeredRules);

  for (const rule of RULES) {
    if (signals[rule.id]) {
      totalScore += rule.score;
      if (!alreadyTracked.has(rule.id)) {
        triggeredRules.push(rule.id);
      }
    }
  }

  // ─────────────────────────────────────────────
  // 최종 판정
  // ─────────────────────────────────────────────
  if (totalScore >= THRESHOLDS.BLOCK)     return buildResult('BLOCK',     totalScore, triggeredRules);
  if (totalScore >= THRESHOLDS.CHALLENGE) return buildResult('CHALLENGE', totalScore, triggeredRules);
  return buildResult('ALLOW', totalScore, triggeredRules);
}

/**
 * Fingerprint 기반 봇 탐지
 * @param {Object} fingerprint - Canvas/WebGL Fingerprint 데이터
 * @returns {Object} { isCritical, score, triggeredRules, reasons }
 */
function checkFingerprint(fingerprint) {
  let score = 0;
  const triggeredRules = [];
  const reasons = [];
  let isCritical = false;

  try {
    // 1. 자동화 도구 직접 탐지 (Critical)
    if (fingerprint.artifacts?.selenium) {
      score += FINGERPRINT_RULES.selenium.score;
      triggeredRules.push('fp_selenium');
      reasons.push('Selenium detected');
      isCritical = true;
    }
    
    if (fingerprint.artifacts?.driver) {
      score += FINGERPRINT_RULES.driver.score;
      triggeredRules.push('fp_driver');
      reasons.push('WebDriver detected');
      isCritical = true;
    }
    
    if (fingerprint.webdriver === true) {
      score += FINGERPRINT_RULES.webdriver.score;
      triggeredRules.push('fp_webdriver');
      reasons.push('WebDriver flag detected');
      isCritical = true;
    }

    // 2. Headless 브라우저 탐지
    const renderer = fingerprint.graphics?.renderer || '';
    const headlessPatterns = ['SwiftShader', 'llvmpipe', 'Mesa', 'ANGLE (Google'];
    
    if (headlessPatterns.some(pattern => renderer.includes(pattern))) {
      score += FINGERPRINT_RULES.headlessRenderer.score;
      triggeredRules.push('fp_headless_renderer');
      reasons.push(`Headless browser suspected (${renderer.substring(0, 50)})`);
    }

    // 3. 플러그인 개수 이상치
    const pluginsCount = fingerprint.browser?.pluginsLength ?? -1;
    if (pluginsCount === 0) {
      score += FINGERPRINT_RULES.noPlugins.score;
      triggeredRules.push('fp_no_plugins');
      reasons.push('No browser plugins');
    }

    // 4. 언어 설정 부재
    const languages = fingerprint.browser?.languages || [];
    if (languages.length === 0) {
      score += FINGERPRINT_RULES.noLanguages.score;
      triggeredRules.push('fp_no_languages');
      reasons.push('No language preferences');
    }

    // 5. Canvas hash 체크 (향후 Redis 연동 시 중복 체크 가능)
    const canvasHash = fingerprint.graphics?.canvas;
    if (canvasHash) {
      // TODO: Redis에서 중복 체크
      // const duplicateCount = await redis.get(`canvas:${canvasHash}`);
      // if (duplicateCount > FINGERPRINT_RULES.canvasDuplicate.threshold) {
      //   score += FINGERPRINT_RULES.canvasDuplicate.score;
      //   triggeredRules.push('fp_canvas_duplicate');
      //   reasons.push(`Canvas hash duplicate (count: ${duplicateCount})`);
      // }
    }

    // 6. 하드웨어 정보 이상치
    const cores = fingerprint.hardware?.cores || 0;
    const memory = fingerprint.hardware?.memory || 0;
    
    if (cores > 64 || cores < 1) {
      score += FINGERPRINT_RULES.abnormalHardware.score;
      triggeredRules.push('fp_abnormal_cores');
      reasons.push(`Abnormal CPU cores: ${cores}`);
    }
    
    if (memory > 128 || memory < 1) {
      score += FINGERPRINT_RULES.abnormalHardware.score;
      triggeredRules.push('fp_abnormal_memory');
      reasons.push(`Abnormal memory: ${memory}GB`);
    }

  } catch (error) {
    console.error('[Fingerprint Check Error]', error.message);
    // Fingerprint 체크 실패 시에도 진행 (가용성 우선)
  }

  return { isCritical, score, triggeredRules, reasons };
}

/**
 * 결과 객체 생성
 * @param {string} action - ALLOW | CHALLENGE | BLOCK
 * @param {number} score - 최종 점수
 * @param {Array} triggeredRules - 발동된 룰 ID 배열
 * @param {Object} metadata - 추가 정보 (optional)
 */
function buildResult(action, score, triggeredRules, metadata = {}) {
  return {
    action,
    score,
    triggeredRules,
    timestamp: new Date().toISOString(),
    ...metadata
  };
}

module.exports = { evaluate, checkFingerprint };