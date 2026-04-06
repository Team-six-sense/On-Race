import axios from 'axios';
import { IEventService } from './interface';

// Next.js API Route를 호출하기 위한 인스턴스
const apiClient = axios.create({
  // 상대 경로를 사용하면 브라우저에서는 현재 도메인(localhost:3000 등)을 자동으로 사용합니다.
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const eventApi: IEventService = {
  getEvents: async () => {
    const response = await apiClient.get('/events');
    return response.data;
  },
  getEventById: async (id) => {
    const response = await apiClient.get(`/events/${id}`);
    return response.data;
  },
  getEventDetails: async (id) => {
    const response = await apiClient.get(`/events/${id}/info`);
    return response.data;
  },
  getSalesInfo: async (id) => {
    const response = await apiClient.get(`/events/${id}/sales-info`);
    return response.data;
  },

  postStockInit: async (id, data) => {
    const response = await apiClient.post(`/events/${id}/stock/init`, data);
    return response.data;
  },
  postQueueEnable: async (id) => {
    const response = await apiClient.post(`/events/${id}/queue/enable`);
    return response.data;
  },
  getQueueEnable: async () => {
    const response = await apiClient.get('/events/queue-enabled');
    return response.data;
  },
  getEventOverview: async (id) => {
    const response = await apiClient.get(`/events/${id}/entries/overview`);
    return response.data;
  },
  getEventRate: async (id, data) => {
    const response = await apiClient.get(`/events/${id}/entries/rate`, {
      params: data,
    });
    return response.data;
  },
  postEventPreSave: async (id, data) => {
    const response = await apiClient.post(
      `/events/${id}/entries/pre-save`,
      data,
    );
    return response.data;
  },
  deleteEventPreSave: async (id) => {
    const response = await apiClient.delete(`/events/${id}/entries/pre-save`);
    return response.data;
  },
  applyEventLottery: async (id, data) => {
    const response = await apiClient.post(
      `/events/${id}/entries/apply/lottery`,
      data,
    );
    return response.data;
  },
  applyEventFirstCome: async (id, data) => {
    const response = await apiClient.post(
      `/events/${id}/entries/apply/lottery`,
      data,
    );
    return response.data;
  },
};
