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
  getEventById(id: number): Promise<ApiResponse<EventList>>;
  getEventDetails(id: number): Promise<ApiResponse<EventDetails>>;
  getSalesInfo(id: number): Promise<ApiResponse<SalesInfo>>;
  postStockInit(
    id: number,
    data: { courseId: number; paceId: number },
  ): Promise<ApiResponse<null>>;
  postQueueEnable(id: number): Promise<ApiResponse<null>>;
  getQueueEnable(): Promise<ApiResponse<number[]>>;
  getEventOverview(id: number): Promise<ApiResponse<EventOverview>>;
  getEventRate(
    id: number,
    data: { courseId: number; paceId: number },
  ): Promise<ApiResponse<EventRate>>;
  postEventPreSave(
    id: number,
    data: { courseId: number; paceId: number },
  ): Promise<ApiResponse<EventPrevSave>>;
  deleteEventPreSave(id: number): Promise<ApiResponse<null>>;
  applyEventLottery(
    id: number,
    data: { courseId: number; paceId: number },
  ): Promise<ApiResponse<EventApply>>;
  applyEventFirstCome(
    id: number,
    data: { courseId: number; paceId: number },
  ): Promise<ApiResponse<EventApply>>;
}
