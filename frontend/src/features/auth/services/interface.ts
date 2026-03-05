import { ApiResponse } from '@/types/api';
import { AccessTokenResponse, LoginResponse, SignupResponse } from '../types';

export interface IAuthService {
  // 회원/인증 API
  signup(): Promise<ApiResponse<SignupResponse>>;
  login(): Promise<ApiResponse<LoginResponse>>;

  logout(): Promise<ApiResponse<void>>;
  deleteAccount(): Promise<ApiResponse<void>>;
  getAccessToken(): Promise<ApiResponse<AccessTokenResponse>>;

  // 이메일 인증 API
  sendEmailCode(): Promise<ApiResponse<void>>;
  verifyEmailCode(): Promise<ApiResponse<void>>;
}
