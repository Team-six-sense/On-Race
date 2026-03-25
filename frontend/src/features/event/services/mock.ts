import { EVENT_LIST, EVENT_DETAILS } from '@/mockups';
import { wrapMockResponse } from '@/utils/api';
import { IEventService } from './interface';
import { SALES_INFO } from '@/mockups/salesInfo';

export const eventMock: IEventService = {
  getEvents: async () => wrapMockResponse(EVENT_LIST),
  getEventById: async (id) => wrapMockResponse(EVENT_LIST),
  getEventDetails: async (id) => wrapMockResponse(EVENT_DETAILS),
  getSalesInfo: async (id) => wrapMockResponse(SALES_INFO),
};
