import { ResultProps } from '@/types/api';

export interface IScheduleService {
  getSchedule(): Promise<ResultProps>;
}
