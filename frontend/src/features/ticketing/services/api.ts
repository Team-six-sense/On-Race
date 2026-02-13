// import { TICKET_INFO } from '@/mockups';
import { wrapMockResponse } from '@/utils/api';
import { ITicketingService } from './interface';

export const ticketingApi: ITicketingService = {
  getTicketingInfo: async () => wrapMockResponse({}),
};
