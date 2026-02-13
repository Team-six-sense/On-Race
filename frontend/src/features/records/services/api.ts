import { MOCK_RUNNER_RECORD } from '@/mockups';
import { wrapMockResponse } from '@/utils/api';
import { IRecordService } from './interface';

export const recordApi: IRecordService = {
  getRecordBySearch: async (query) => wrapMockResponse(MOCK_RUNNER_RECORD),
};
