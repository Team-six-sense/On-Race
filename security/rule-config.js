/**
 * rules-config.js
 * 탐지 룰 및 점수 설정
 */

module.exports = {
  // 최종 판정 임계값
  THRESHOLDS: {
    BLOCK: 85,
    CHALLENGE: 50
  },

  // 1단계: 즉시 차단 그룹
  CRITICAL_AUTOMATION_GROUP: ['webdriver', 'seleniumArtifact', 'headlessFlag'],
  CRITICAL_SINGLE_RULES: ['uaBotKeyword', 'honeypot'],

  // 2단계: SwiftShader 조합 설정
  SWIFT_SHADER_COMBOS: {
    baseScore: 60,
    blockThreshold: 120,
    bonusScores: {
      noMouse: 55,
      uaStructure: 80,
      noFocusBlur: 65
    }
  },

  // 3단계: 일반 룰 리스트
  RULES: [
    // 그룹 A: Static / Fingerprint
    { id: 'uaStructure', score: 80 },
    { id: 'noPluginsMemory', score: 70 },
    { id: 'swiftShader', score: 60 },
    { id: 'canvasChange', score: 45 },
    { id: 'lowTextureSize', score: 30 },

    // 그룹 B: 오픈 타이밍 / 속도
    { id: 'submitWithin3s', score: 70 },
    { id: 'submitWithin800ms', score: 65 },
    { id: 'inputUnder1s', score: 60 },
    { id: 'noMouse', score: 55 },
    { id: 'uniformInputGap', score: 50 },

    // 그룹 C: 행동 패턴
    { id: 'fastRetry', score: 70 },
    { id: 'noFocusBlur', score: 65 },
    { id: 'noScroll', score: 50 },

    // 그룹 D: 네트워크/서버 판단
    { id: 'canvasRateLimit', score: 60 },
    { id: 'datacenterIp', score: 40 },

    // 그룹 E: 입력 방식
    { id: 'keydownMismatch', score: 60 },
    { id: 'clipboardOveruse', score: 40 },

    // 그룹 F: 세션 정합성
    { id: 'referrerInvalid', score: 80 },
    { id: 'backgroundTab', score: 45 },

    // 그룹 G: 환경 일관성
    { id: 'touchUaMismatch', score: 50 },
    { id: 'batteryMismatch', score: 35 }
  ]
};