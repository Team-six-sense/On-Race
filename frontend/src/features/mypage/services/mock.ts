import { wrapMockResponse } from '@/utils/api';
import { IMypageService } from './interface';
import {
  MOCK_ACCOUNT_INFO,
  MOCK_EVENT_HISTORY,
  MOCK_PAYMENT_HISTORY,
} from '@/mockups';

export const mypageMock: IMypageService = {
  getAccountInfo: async (id) => wrapMockResponse(MOCK_ACCOUNT_INFO),
  getEventHistory: async (id) => wrapMockResponse(MOCK_EVENT_HISTORY),
  getPaymentHistory: async (id) => wrapMockResponse(MOCK_PAYMENT_HISTORY),
};
