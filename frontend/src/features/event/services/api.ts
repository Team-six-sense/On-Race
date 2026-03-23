import axios from 'axios';
import { ApiResponse } from '@/types/api';
import { EventDetails, EventList } from '../types';
import { IEventService } from './interface';

// Next.js API Route를 호출하기 위한 인스턴스
const apiClient = axios.create({
  // 상대 경로를 사용하면 브라우저에서는 현재 도메인(localhost:3000 등)을 자동으로 사용합니다.
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const eventApi: IEventService = {
  getEvents: async () => {
    const response = await apiClient.get<ApiResponse<EventList>>('/events');
    return response.data;
  },
  getEventById: async (id) => {
    const response = await apiClient.get<ApiResponse<EventList>>('/events');
    return response.data;
  },
  getEventDetails: async (id) => {
    const response = await apiClient.get<ApiResponse<EventDetails>>('/events');
    return response.data;
  },
};
