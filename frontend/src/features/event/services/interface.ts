import { ApiResponse } from '@/types/api';
import { EventList, EventDetails, SalesInfo } from '../types';

export interface IEventService {
  getEvents(): Promise<ApiResponse<EventList>>;
  getEventById(id: number): Promise<ApiResponse<EventList>>;
  getEventDetails(id: number): Promise<ApiResponse<EventDetails>>;
  getSalesInfo(id: number): Promise<ApiResponse<SalesInfo>>;
}
