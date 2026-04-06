import axios from 'axios';
import { wrapMockResponse } from '@/utils/api';
import { ITicketingService } from './interface';

// Next.js API Route를 호출하기 위한 인스턴스
const apiClient = axios.create({
  // 상대 경로를 사용하면 브라우저에서는 현재 도메인(localhost:3000 등)을 자동으로 사용합니다.
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const ticketingApi: ITicketingService = {
  enterQueue: async (data) => {
    const response = await apiClient.post('/queue/enter', data);
    return response.data;
  },

  getQueueStatus: async (data) => {
    const response = await apiClient.get(`/queue/status`, {
      params: data,
    });
    return response.data;
  },
  leaveQueue: async (data) => {
    const response = await apiClient.delete(`/queue/leave`, {
      params: data,
    });
    return response.data;
  },
};
