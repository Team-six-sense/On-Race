import axios from 'axios';
import { ApiResponse } from '@/types/api';
import { IAuthService } from './interface';
import {
  AccessTokenResponse,
  FindAccountResponse,
  LoginResponse,
  SignupResponse,
} from '../types';

// Next.js API Route를 호출하기 위한 인스턴스
export const apiClient = axios.create({
  // 환경 변수에서 가져오거나, 없을 경우 로컬 주소를 기본값으로 설정
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3000/api',
  headers: {
    'Content-Type': 'application/json',
  },
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
    const response = await apiClient.post<ApiResponse<SignupResponse>>(
      '/auth/signup',
      data,
    );
    return response.data;
  },
  login: async (data) => {
    const response = await apiClient.post<ApiResponse<LoginResponse>>(
      '/auth/login',
      data,
    );
    return response.data;
  },
  logout: async () => {
    const response = await apiClient.post<ApiResponse<void>>('/auth/logout');
    return response.data;
  },

  findAccount: async (data) => {
    const response = await apiClient.post<ApiResponse<FindAccountResponse>>(
      '/auth/find-email',
      data,
    );
    return response.data;
  },
  deleteAccount: async () => {
    const response = await apiClient.delete<ApiResponse<void>>('/auth/account');
    return response.data;
  },
  checkEmailAddress: async (data) => {
    const response = await apiClient.get<ApiResponse<void>>(
      '/auth/check-email',
      { params: data },
    );
    return response.data;
  },

  // 패스워드 API
  resetPassword: async (data) => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/auth/password/reset',
      data,
    );
    return response.data;
  },
  sendPasswordResetLink: async (data) => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/auth/password/reset-request',
      data,
    );
    return response.data;
  },

  verifyPasswordResetLink: async (data) => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/auth/password/reset-verify',
      data,
    );
    return response.data;
  },

  // 토큰 API
  getAccessToken: async (data) => {
    const response = await apiClient.post<ApiResponse<AccessTokenResponse>>(
      '/auth/token/refresh',
      data,
    );
    return response.data;
  },

  // 이메일 API
  sendEmailCode: async (data) => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/auth/email/send-code',
      data,
    );
    return response.data;
  },
  verifyEmailCode: async (data) => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/auth/email/verify-code',
      data,
    );
    return response.data;
  },

  // SMS API
  sendSmsCode: async (data) => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/auth/sms/send',
      data,
    );
    return response.data;
  },
  verifySmsCode: async (data) => {
    const response = await apiClient.post<ApiResponse<void>>(
      '/auth/sms/verify',
      data,
    );
    return response.data;
  },
};
