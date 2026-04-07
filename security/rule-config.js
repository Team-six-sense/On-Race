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

  // 3단계: Fingerprint 전용 룰 설정
  FINGERPRINT_RULES: {
    // Critical (자동화 도구 직접 탐지 - 즉시 BLOCK)
    selenium: { score: 100, description: 'Selenium automation tool detected' },
    driver: { score: 100, description: 'WebDriver detected' },
    webdriver: { score: 100, description: 'WebDriver flag detected' },
    
    // High suspicious
    headlessRenderer: { score: 40, description: 'Headless browser renderer (SwiftShader/llvmpipe/Mesa)' },
    
    // Medium suspicious
    noPlugins: { score: 15, description: 'No browser plugins installed' },
    noLanguages: { score: 10, description: 'No language preferences set' },
    abnormalHardware: { score: 20, description: 'Abnormal hardware specifications' },
    
    // Canvas duplicate (Redis 연동 필요)
    canvasDuplicate: { 
      score: 30, 
      threshold: 5, // 동일 hash 5회 이상 시 의심
      description: 'Canvas fingerprint duplicate detected' 
    }
  },

  // 4단계: 일반 룰 리스트
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