import { ApiResponse } from '@/types/api';
import { EnterQueue, QueueStatus } from '../types';

export interface ITicketingService {
  enterQueue(data: { pace: number }): Promise<ApiResponse<EnterQueue>>;
  getQueueStatus(data: {
    courseId: number;
    paceId: number;
  }): Promise<ApiResponse<QueueStatus>>;
  leaveQueue(data: { paceId: number }): Promise<ApiResponse<null>>;
}
