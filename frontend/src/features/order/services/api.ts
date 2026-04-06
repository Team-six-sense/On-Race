import axios from 'axios';
import { IOrderService } from './interface';

// Next.js API Route를 호출하기 위한 인스턴스
const apiClient = axios.create({
  // 상대 경로를 사용하면 브라우저에서는 현재 도메인(localhost:3000 등)을 자동으로 사용합니다.
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const orderApi: IOrderService = {
  getOrder: async () => {
    const response = await apiClient.get('/orders');
    return response.data;
  },
  getOrderDetails: async (id) => {
    const response = await apiClient.get(`/orders/${id}`);
    return response.data;
  },
  postOrderConfirm: async (id) => {
    const response = await apiClient.post(`/orders/${id}/confirm`);
    return response.data;
  },
  postOrderCheckout: async (data) => {
    const response = await apiClient.post(`/orders/checkout`, data);
    return response.data;
  },
  postOrderCheckoutInfo: async (data) => {
    const response = await apiClient.post(`/orders/checkout-info`, data);
    return response.data;
  },
  postPaymentConfirm: async (data) => {
    const response = await apiClient.post(`/payments/confirm`, data);
    return response.data;
  },
};
