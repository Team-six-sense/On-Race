import { wrapMockResponse } from '@/utils/api';
import { IAuthService } from './interface';

import {
  ACCESS_TOKEN_RESPONSE,
  LOGIN_RESPONSE,
  SIGNUP_RESPONSE,
} from '@/mockups';

export const authMock: IAuthService = {
  // 회원/인증 API
  signup: async (data) => wrapMockResponse(SIGNUP_RESPONSE),
  login: async () => wrapMockResponse(LOGIN_RESPONSE),
  logout: async () => wrapMockResponse(),
  deleteAccount: async () => wrapMockResponse(),
  getAccessToken: async () => wrapMockResponse(ACCESS_TOKEN_RESPONSE),

  // 이메일 인증 API
  sendEmailCode: async () => wrapMockResponse(),
  verifyEmailCode: async () => wrapMockResponse(),
};
