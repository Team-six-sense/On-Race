import {
  AccessTokenResponse,
  LoginResponse,
  SignupResponse,
} from '@/features/auth/types';

export const SIGNUP_RESPONSE: SignupResponse = {
  id: 1,
  email: '서울 마라톤 2026',
  createAt: '2026-02-01T00:00:00',
};

export const LOGIN_RESPONSE: LoginResponse = {
  accessToken: 'MOCK_ACCESS_TOKEN',
  refreshToken: 'MOCK_REFRESH_TOKEN',
  tokenType: 'Bearer',
  expiresIn: 1000000,
};

export const ACCESS_TOKEN_RESPONSE: AccessTokenResponse = {
  accessToken: 'MOCK_ACCESS_TOKEN',
  expiresIn: 1000000,
};
