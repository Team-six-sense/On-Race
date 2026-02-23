import { ResultProps } from '@/types/api';

export interface ITicketingService {
  getTicketingInfo(): Promise<ResultProps>;
}
