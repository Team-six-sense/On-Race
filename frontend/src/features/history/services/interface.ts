import { ResultProps } from '@/types/api';

export interface IHistoryService {
  getRecord(search: string): Promise<ResultProps>;
  getApplication(search: string): Promise<ResultProps>;
}
