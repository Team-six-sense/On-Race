import { wrapMockResponse } from '@/utils/api';
import { IAuthService } from './interface';

import {
  ACCESS_TOKEN_RESPONSE,
  FIND_ACCOUNT_RESPONSE,
  LOGIN_RESPONSE,
  SIGNUP_RESPONSE,
} from '@/mockups';

export const authMock: IAuthService = {
  // 회원/인증 API
  signup: async (data) => wrapMockResponse(SIGNUP_RESPONSE),
  login: async () => wrapMockResponse(LOGIN_RESPONSE),
  logout: async () => wrapMockResponse(),
  findAccount: async (data) => wrapMockResponse(FIND_ACCOUNT_RESPONSE),
  deleteAccount: async () => wrapMockResponse(),
  checkEmailAddress: async (data) => wrapMockResponse(),

  // password API
  resetPassword: async (data) => wrapMockResponse(),
  sendPasswordResetLink: async (data) => wrapMockResponse(),
  verifyPasswordResetLink: async (data) => wrapMockResponse(),

  // 토큰 API
  getAccessToken: async () => wrapMockResponse(ACCESS_TOKEN_RESPONSE),

  // 이메일 인증 API
  sendEmailCode: async (data) => wrapMockResponse(),
  verifyEmailCode: async (data) => wrapMockResponse(),

  // SMS 인증 API
  sendSmsCode: async (data) => wrapMockResponse(),
  verifySmsCode: async (data) => wrapMockResponse(),
};
