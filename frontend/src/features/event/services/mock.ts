import {
  EVENT_LIST,
  EVENT_DETAILS,
  SALES_INFO,
  EVENT_OVERVIEW,
  EVENT_RATE,
  EVENT_PREV_SAVE,
  EVENT_APPLY,
} from '@/mockups';
import { wrapMockResponse } from '@/utils/api';
import { IEventService } from './interface';

export const eventMock: IEventService = {
  getEvents: async () => wrapMockResponse(EVENT_LIST),
  getEventById: async (id) => wrapMockResponse(EVENT_LIST),
  getEventDetails: async (id) => wrapMockResponse(EVENT_DETAILS),
  getSalesInfo: async (id) => wrapMockResponse(SALES_INFO),
  postStockInit: async (id) => wrapMockResponse(),
  postQueueEnable: async (id) => wrapMockResponse(),
  getQueueEnable: async () => wrapMockResponse([1, 3, 5]),
  getEventOverview: async (id) => wrapMockResponse(EVENT_OVERVIEW),
  getEventRate: async (id, data) => wrapMockResponse(EVENT_RATE),
  postEventPreSave: async (id, data) => wrapMockResponse(EVENT_PREV_SAVE),
  deleteEventPreSave: async (id) => wrapMockResponse(),
  applyEventLottery: async (id, data) => wrapMockResponse(EVENT_APPLY),
  applyEventFirstCome: async (id, data) => wrapMockResponse(EVENT_APPLY),
};
