import axios from 'axios';
import { ApiResponse } from '@/types/api';
import { IMypageService } from './interface';
import { AccountInfo } from '../types/accountInfo';
import { EventHistory } from '../types/eventHistory';
import { PaymentHistory } from '../types/paymentHistory';

// Next.js API Route를 호출하기 위한 인스턴스
const apiClient = axios.create({
  // 상대 경로를 사용하면 브라우저에서는 현재 도메인(localhost:3000 등)을 자동으로 사용합니다.
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const mypageApi: IMypageService = {
  getAccountInfo: async (id) => {
    const response = await apiClient.get<ApiResponse<AccountInfo>>(
      '/mypage/accountInfo',
      {
        params: { id },
      },
    );
    return response.data;
  },
  getEventHistory: async (id) => {
    const response = await apiClient.get<ApiResponse<EventHistory[]>>(
      `/mypage/eventHistory`,
      {
        params: { id },
      },
    );
    return response.data;
  },
  getPaymentHistory: async (id) => {
    const response = await apiClient.get<ApiResponse<PaymentHistory[]>>(
      `/mypage/paymentHistory`,
      {
        params: { id },
      },
    );
    return response.data;
  },
};
