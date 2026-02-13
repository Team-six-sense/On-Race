export interface QueueStatus {
  position: number; // 내 대기 순번
  totalWaiting: number; // 전체 대기 인원
  expectedWaitTime: string; // 예상 대기 시간 (예: "05:30")
  status: 'waiting' | 'passed' | 'error';
  accessToken?: string; // 대기열 통과 시 발급받는 보안 토큰
}
