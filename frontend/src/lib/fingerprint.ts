export const collectFingerprint = () => {
  if (typeof window === 'undefined') return null;

  return {
    // 1. 자동화 도구 탐지 (Webdriver)
    webdriver: navigator.webdriver,

    // 2. Selenium Artifacts (흔적)
    artifacts: {
      selenium: ['__webdriver_evaluate', '__selenium_evaluate'].some(
        (key) => key in window,
      ),
      driver: !!(window as any)._phantom || !!(window as any).__nightmare,
    },

    // 3. Headless Detection 및 환경 정보
    browser: {
      ua: navigator.userAgent,
      platform: navigator.platform,
      languages: navigator.languages || [],
      pluginsLength: navigator.plugins.length,
    },

    // 4. User-Agent 정규화용 하드웨어 정보
    hardware: {
      cores: navigator.hardwareConcurrency || 0,
      memory: (navigator as any).deviceMemory || 0,
    },

    // 5. 그래픽 지문 (WebGL & Canvas)
    graphics: {
      renderer: getWebGLRenderer(),
      canvas: getCanvasFingerprint(),
    },
  };
};

function simpleHash(str: string) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = (hash << 5) - hash + char;
    hash |= 0; // 32비트 정수로 변환
  }
  return hash.toString(16);
}

function getWebGLRenderer() {
  const canvas = document.createElement('canvas');
  const gl = canvas.getContext('webgl');
  if (!gl) return 'unknown';
  const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
  return debugInfo
    ? gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL)
    : 'unknown';
}

function getCanvasFingerprint() {
  try {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    if (!ctx) return 'unknown';

    // 1. 캔버스 크기 및 기본 설정
    canvas.width = 200;
    canvas.height = 50;
    ctx.textBaseline = 'top';

    // 2. 다양한 폰트와 스타일을 혼합하여 렌더링 (차이 극대화)
    ctx.font = "14px 'Arial'";
    ctx.fillStyle = '#f60';
    ctx.fillRect(125, 1, 62, 20);
    ctx.fillStyle = '#069';
    ctx.fillText('Fingerprint, 123!', 2, 15);
    ctx.fillStyle = 'rgba(102, 204, 0, 0.7)';
    ctx.fillText('Fingerprint, 123!', 4, 17);

    // 3. 이미지 데이터를 Base64로 변환
    const result = canvas.toDataURL();

    // 4. 너무 긴 문자열 대신 간단한 해시로 변환 (선택 사항)
    return simpleHash(result);
  } catch (e) {
    return 'error';
  }
}
