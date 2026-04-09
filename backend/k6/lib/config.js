// ========================================================
// k6 부하테스트 공통 설정
// 모든 값은 환경변수(-e)로 오버라이드 가능
// ========================================================

// === 서버 설정 ===
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:30000';

// === 시나리오별 이벤트 ID (setup-load-test-events.sql 기준) ===
export const EVENT_IDS = {
  LOTTERY:                parseInt(__ENV.EVENT_LOTTERY || '11'),
  FIRST_COME_NO_QUEUE:   parseInt(__ENV.EVENT_FC_NO_QUEUE || '12'),
  FIRST_COME_WITH_QUEUE: parseInt(__ENV.EVENT_FC_WITH_QUEUE || '13'),
};

// === 유저 설정 (최대 30,000명) ===
export const VU_COUNT      = parseInt(__ENV.VU_COUNT || '100');
export const USER_PASSWORD = __ENV.USER_PASSWORD || 'Test1234!@';

// === 로그인 설정 ===
export const LOGIN_TIMEOUT    = __ENV.LOGIN_TIMEOUT || '120s';
export const LOGIN_BATCH_SIZE = parseInt(__ENV.LOGIN_BATCH_SIZE || '50');

// === 페이스 배분 (70% HOT, 30% 기타) ===
export const HOT_PACE_RATIO = parseFloat(__ENV.HOT_PACE_RATIO || '0.7');

// === Setup 타임아웃 (데이터 셋업 + 로그인) ===
export const SETUP_TIMEOUT_SEC = parseInt(__ENV.SETUP_TIMEOUT_SEC || '600');

// === 부하 패턴 (ramping-vus) ===
export const RAMP_UP_SEC   = parseInt(__ENV.RAMP_UP_SEC || '10');
export const HOLD_SEC      = parseInt(__ENV.HOLD_SEC || '120');
export const RAMP_DOWN_SEC = parseInt(__ENV.RAMP_DOWN_SEC || '5');

// === 재고 설정 ===
export const TOTAL_STOCK       = __ENV.TOTAL_STOCK ? parseInt(__ENV.TOTAL_STOCK) : null;  // 직접 지정 (null이면 자동 계산)
export const COMPETITION_RATIO = parseInt(__ENV.COMPETITION_RATIO || '3');     // 경쟁률 (3:1) — TOTAL_STOCK 미지정 시 사용
export const HOT_PACE_COUNT    = parseInt(__ENV.HOT_PACE_COUNT || '2');        // 인기 페이스 수 (랜덤 선정)
export const HOT_STOCK_RATIO   = parseFloat(__ENV.HOT_STOCK_RATIO || '0.4');  // 인기 페이스 재고 비율 (40%)

// === 인기 페이스 고정 지정 (두 값 모두 지정 시 해당 코스-페이스 1개를 hot으로 고정) ===
export const HOT_COURSE_INDEX = __ENV.HOT_COURSE_INDEX != null ? parseInt(__ENV.HOT_COURSE_INDEX) : null;  // 0=풀코스, 1=하프��스, 2=10km
export const HOT_PACE_INDEX   = __ENV.HOT_PACE_INDEX != null ? parseInt(__ENV.HOT_PACE_INDEX) : null;      // 0=3분, 1=4분, 2=5분, 3=6분, 4=7분

// === 사전정보 저장 비율 ===
export const PRE_SAVE_RATIO = __ENV.PRE_SAVE_RATIO != null ? parseFloat(__ENV.PRE_SAVE_RATIO) : null;  // null이면 건너뜀, 0.7이면 70%

// === Queue 폴링 설정 ===
export const QUEUE_POLL_INTERVAL_SEC = parseInt(__ENV.QUEUE_POLL_SEC || '2');
export const QUEUE_MAX_POLL_COUNT    = parseInt(__ENV.QUEUE_MAX_POLL || '300');

// === 결제 이탈 + 2웨이브 설정 ===
export const PAYMENT_DROPOUT_RATIO = parseFloat(__ENV.PAYMENT_DROPOUT_RATIO || '0.3');  // 30% 이탈
export const RETRY_MAX_ROUNDS      = parseInt(__ENV.RETRY_MAX_ROUNDS || '5');            // 매진 시 최대 재시도 횟수
export const RETRY_WAIT_SEC        = parseInt(__ENV.RETRY_WAIT_SEC || '18');             // 재시도 대기 (TTL 15초 + 여유 3초)

// === Wave 2 추가 인원 (이탈자 재고 재선점) ===
export const EXTRA_VU_COUNT = __ENV.EXTRA_VU_COUNT != null
  ? parseInt(__ENV.EXTRA_VU_COUNT)
  : Math.ceil(VU_COUNT * PAYMENT_DROPOUT_RATIO);  // 미지정 시 이탈률 기반 자동 계산
