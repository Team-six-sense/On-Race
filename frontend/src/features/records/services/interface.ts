import { ResultProps } from '@/types/api';

export interface IRecordService {
  getRecordBySearch(search: string): Promise<ResultProps>;
}
