export interface QueueResponse {
  position: number;
  totalWaiting: number;
  expectedWaitTime: number;
  status: 'waiting' | 'passed';
  accessToken: string | null;
}
