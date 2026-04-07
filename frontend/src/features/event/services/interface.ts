import { ApiResponse } from '@/types/api';
import {
  EventList,
  EventDetails,
  SalesInfo,
  EventOverview,
  EventRate,
  EventPrevSave,
  EventApply,
} from '../types';

export interface IEventService {
  getEvents(): Promise<ApiResponse<EventList>>;
  getEventById(id: string): Promise<ApiResponse<EventList>>;
  getEventDetails(id: string): Promise<ApiResponse<EventDetails>>;
  getSalesInfo(id: string): Promise<ApiResponse<SalesInfo>>;
  postStockInit(
    id: string,
    data: { courseId: number; paceId: number },
  ): Promise<ApiResponse<null>>;
  postQueueEnable(id: string): Promise<ApiResponse<null>>;
  getQueueEnable(): Promise<ApiResponse<number[]>>;
  getEventOverview(id: string): Promise<ApiResponse<EventOverview>>;
  getEventRate(
    id: string,
    data: { courseId: number; paceId: number },
  ): Promise<ApiResponse<EventRate>>;
  postEventPreSave(
    id: string,
    data: { courseId: number; paceId: number },
  ): Promise<ApiResponse<EventPrevSave>>;
  deleteEventPreSave(id: string): Promise<ApiResponse<null>>;
  applyEventLottery(
    id: string,
    data: { courseId: number; paceId: number },
  ): Promise<ApiResponse<EventApply>>;
  applyEventFirstCome(
    id: string,
    data: { courseId: number; paceId: number },
  ): Promise<ApiResponse<EventApply>>;
}
