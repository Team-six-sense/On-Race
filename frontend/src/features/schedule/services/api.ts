import { MARATHON_DATA } from '@/mockups';
import { wrapMockResponse } from '@/utils/api';
import { IScheduleService } from './interface';

export const scheduleApi: IScheduleService = {
  getSchedule: async () => wrapMockResponse(MARATHON_DATA),
};
