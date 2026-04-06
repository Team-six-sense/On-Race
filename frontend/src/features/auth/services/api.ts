import axios from 'axios';
import { IAuthService } from './interface';

// Next.js API Route를 호출하기 위한 인스턴스
const apiClient = axios.create({
  // 상대 경로를 사용하면 브라우저에서는 현재 도메인(localhost:3000 등)을 자동으로 사용합니다.
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// apiClient.interceptors.request.use((config) => {
// 로컬 스토리지나 상태 관리 라이브러리에서 토큰/ID 가져오기
// const token =
//   typeof window !== 'undefined' ? localStorage.getItem('token') : null;
// if (token) {
//   config.headers.Authorization = `Bearer ${token}`;
// }
// return config;
// });

export const authApi: IAuthService = {
  // 회원 API
  signup: async (data) => {
    const response = await apiClient.post('/auth/signup', data);
    return response.data;
  },
  login: async (data) => {
    const response = await apiClient.post('/auth/login', data);
    return response.data;
  },
  logout: async () => {
    const response = await apiClient.post('/auth/logout');
    return response.data;
  },

  findAccount: async (data) => {
    const response = await apiClient.post('/auth/find-email', data);
    return response.data;
  },
  deleteAccount: async () => {
    const response = await apiClient.delete('/auth/account');
    return response.data;
  },
  checkEmailAddress: async (data) => {
    const response = await apiClient.get('/auth/check-email', { params: data });
    return response.data;
  },

  // 약관 동의
  getTerms: async () => {
    const response = await apiClient.get('/auth/terms');
    return response.data;
  },
  getTermDetails: async (id) => {
    const response = await apiClient.get(`/auth/terms/${id}`);
    return response.data;
  },

  // 패스워드 API
  resetPassword: async (data) => {
    const response = await apiClient.post('/auth/password/reset', data);
    return response.data;
  },
  sendPasswordResetLink: async (data) => {
    const response = await apiClient.post('/auth/password/reset-request', data);
    return response.data;
  },

  verifyPasswordResetLink: async (data) => {
    const response = await apiClient.post('/auth/password/reset-verify', data);
    return response.data;
  },

  // 토큰 API
  getAccessToken: async (data) => {
    const response = await apiClient.post('/auth/token/refresh', data);
    return response.data;
  },

  // 이메일 API
  sendEmailCode: async (data) => {
    const response = await apiClient.post('/auth/email/send-code', data);
    return response.data;
  },
  verifyEmailCode: async (data) => {
    const response = await apiClient.post('/auth/email/verify-code', data);
    return response.data;
  },

  // SMS API
  sendSmsCode: async (data) => {
    const response = await apiClient.post('/auth/sms/send', data);
    return response.data;
  },
  verifySmsCode: async (data) => {
    const response = await apiClient.post('/auth/sms/verify', data);
    return response.data;
  },
};
