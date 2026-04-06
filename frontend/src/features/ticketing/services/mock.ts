import { wrapMockResponse } from '@/utils/api';
import { ITicketingService } from './interface';
import { QUEUE_STATUS } from '@/mockups';

export const ticketingMock: ITicketingService = {
  enterQueue: async () => wrapMockResponse({ paceId: 3, position: 142 }),
  getQueueStatus: async () => wrapMockResponse(QUEUE_STATUS),
  leaveQueue: async () => wrapMockResponse(),
};
