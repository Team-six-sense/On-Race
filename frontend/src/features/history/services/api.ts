import { MOCK_RECORD_DATA, MOCK_APPLICATIONS } from '@/mockups';
import { wrapMockResponse } from '@/utils/api';
import { IHistoryService } from './interface';

export const historyApi: IHistoryService = {
  getRecord: async (query) => wrapMockResponse(MOCK_RECORD_DATA),
  getApplication: async (query) => wrapMockResponse(MOCK_APPLICATIONS),
};
