import axios from 'axios';
import { ApiResponse } from '@/types/api';
import { IAuthService } from './interface';
import { AccessTokenResponse, LoginResponse, SignupResponse } from '../types';

// Next.js API Route를 호출하기 위한 인스턴스
const apiClient = axios.create({
  // 상대 경로를 사용하면 브라우저에서는 현재 도메인(localhost:3000 등)을 자동으로 사용합니다.
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const authApi: IAuthService = {
  // 회원/인증 API
  signup: async () => {
    const response =
      await apiClient.post<ApiResponse<SignupResponse>>('/signup');
    return response.data;
  },
  login: async () => {
    const response = await apiClient.post<ApiResponse<LoginResponse>>('/login');
    return response.data;
  },
  logout: async () => {
    const response = await apiClient.post<ApiResponse<void>>('/logout');
    return response.data;
  },
  deleteAccount: async () => {
    const response = await apiClient.delete<ApiResponse<void>>('/account');
    return response.data;
  },
  getAccessToken: async () => {
    const response =
      await apiClient.post<ApiResponse<AccessTokenResponse>>('/token/refresh');
    return response.data;
  },

  // 이메일 API
  sendEmailCode: async () => {
    const response = await apiClient.post<ApiResponse<void>>('/email/sendCode');
    return response.data;
  },
  verifyEmailCode: async () => {
    const response =
      await apiClient.post<ApiResponse<void>>('/email/verifyCode');
    return response.data;
  },
};
