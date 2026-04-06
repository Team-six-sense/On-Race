import { wrapMockResponse } from '@/utils/api';
import { IMypageService } from './interface';
import {
  MOCK_ACCOUNT_INFO,
  MOCK_EVENT_HISTORY,
  MOCK_PAYMENT_HISTORY,
} from '@/mockups';

export const mypageMock: IMypageService = {
  getAccountInfo: async () => wrapMockResponse(MOCK_ACCOUNT_INFO),
  getHistoryOverview: async () => wrapMockResponse(),
  getEntriesHistory: async () => wrapMockResponse(MOCK_EVENT_HISTORY),
  getWaitingHistory: async () => wrapMockResponse(),
  getOrderHistory: async () => wrapMockResponse(MOCK_PAYMENT_HISTORY),
  getOrderDetailInfo: async (id) => wrapMockResponse(),
};
