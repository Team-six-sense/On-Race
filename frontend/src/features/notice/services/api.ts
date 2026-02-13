import { MOCK_NOTICES } from '@/mockups';
import { wrapMockResponse } from '@/utils/api';
import { INoticeService } from './interface';

export const noticeApi: INoticeService = {
  getNotices: async () => wrapMockResponse(MOCK_NOTICES),
};
