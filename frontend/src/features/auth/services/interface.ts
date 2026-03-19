import { ApiResponse } from '@/types/api';
import {
  AccessTokenRequest,
  AccessTokenResponse,
  EmailSendCodeRequest,
  EmailVerifyCodeRequest,
  FindAccountRequest,
  FindAccountResponse,
  LoginRequest,
  LoginResponse,
  SendPasswordResetLinkRequest,
  SignupRequest,
  SignupResponse,
  resetPasswordRequest,
  VerifyPasswordLinkRequest,
  CheckEmailAddressRequest,
  SmsSendCodeRequest,
  SmsVerifyCodeRequest,
} from '../types';

export interface IAuthService {
  // 회원 API
  signup(data: SignupRequest): Promise<ApiResponse<SignupResponse>>;
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>>;
  logout(): Promise<ApiResponse<void>>;
  findAccount(
    data: FindAccountRequest,
  ): Promise<ApiResponse<FindAccountResponse>>;
  deleteAccount(): Promise<ApiResponse<void>>;
  checkEmailAddress(data: CheckEmailAddressRequest): Promise<ApiResponse<void>>;

  // 패스워드 API
  resetPassword(data: resetPasswordRequest): Promise<ApiResponse<void>>;
  sendPasswordResetLink(
    data: SendPasswordResetLinkRequest,
  ): Promise<ApiResponse<void>>;
  verifyPasswordResetLink(
    data: VerifyPasswordLinkRequest,
  ): Promise<ApiResponse<void>>;

  // 토큰 인증 API
  getAccessToken(
    data: AccessTokenRequest,
  ): Promise<ApiResponse<AccessTokenResponse>>;

  // 이메일 인증 API
  sendEmailCode(data: EmailSendCodeRequest): Promise<ApiResponse<void>>;
  verifyEmailCode(data: EmailVerifyCodeRequest): Promise<ApiResponse<void>>;

  // SMS 인증 API
  sendSmsCode(data: SmsSendCodeRequest): Promise<ApiResponse<void>>;
  verifySmsCode(data: SmsVerifyCodeRequest): Promise<ApiResponse<void>>;
}
