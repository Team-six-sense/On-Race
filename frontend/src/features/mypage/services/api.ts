import axios from 'axios';
import { IMypageService } from './interface';

// Next.js API Route를 호출하기 위한 인스턴스
const apiClient = axios.create({
  // 상대 경로를 사용하면 브라우저에서는 현재 도메인(localhost:3000 등)을 자동으로 사용합니다.
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const mypageApi: IMypageService = {
  getAccountInfo: async () => {
    const response = await apiClient.get('/mypage/accountInfo');
    return response.data;
  },
  getHistoryOverview: async () => {
    const response = await apiClient.get(`/mypage`);
    return response.data;
  },
  getEntriesHistory: async () => {
    const response = await apiClient.get(`/mypage/entries`);
    return response.data;
  },
  getWaitingHistory: async () => {
    const response = await apiClient.get(`/mypage/waiting-entries`);
    return response.data;
  },
  getOrderHistory: async () => {
    const response = await apiClient.get(`/mypage/orders`);
    return response.data;
  },
  getOrderDetailInfo: async (id) => {
    const response = await apiClient.get(`/mypage/orders/${id}`);
    return response.data;
  },
};
