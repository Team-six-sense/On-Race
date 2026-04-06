export interface QueueStatus {
  paceId: number;
  position: number;
  totalWaiting: number;
  expectedWaitTime: number;
  status: 'waiting' | 'passed';
  passToken: string | null;
}
