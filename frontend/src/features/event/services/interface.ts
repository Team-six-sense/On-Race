import { ApiResponse } from '@/types/api';
import { EventList, EventDetails } from '../types';

export interface IEventService {
  getEvents(): Promise<ApiResponse<EventList>>;
  getEventById(id: number): Promise<ApiResponse<EventList>>;
  getEventDetails(id: number): Promise<ApiResponse<EventDetails>>;
}
