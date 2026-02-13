import { ResultProps } from '@/types/api';

export interface INoticeService {
  getNotices(): Promise<ResultProps>;
}
