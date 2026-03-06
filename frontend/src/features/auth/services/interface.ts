import { ApiResponse } from '@/types/api';
import {
  AccessTokenRequest,
  AccessTokenResponse,
  EmailSendCodeRequest,
  EmailVerifyCodeRequest,
  LoginRequest,
  LoginResponse,
  SignupRequest,
  SignupResponse,
} from '../types';

export interface IAuthService {
  // 회원/인증 API
  signup(data: SignupRequest): Promise<ApiResponse<SignupResponse>>;
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>>;

  logout(): Promise<ApiResponse<void>>;
  deleteAccount(): Promise<ApiResponse<void>>;
  getAccessToken(
    data: AccessTokenRequest,
  ): Promise<ApiResponse<AccessTokenResponse>>;

  // 이메일 인증 API
  sendEmailCode(data: EmailSendCodeRequest): Promise<ApiResponse<void>>;
  verifyEmailCode(data: EmailVerifyCodeRequest): Promise<ApiResponse<void>>;
}
