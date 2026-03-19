import {
  AccessTokenResponse,
  FindAccountResponse,
  LoginResponse,
  SignupResponse,
} from '@/features/auth/types';

export const SIGNUP_RESPONSE: SignupResponse = {
  id: 1,
  email: 'example@email.com',
  createAt: '2026-02-01T00:00:00',
};

export const LOGIN_RESPONSE: LoginResponse = {
  id: 'USER_01',
  name: '김유저',
  email: 'example@email.com',
  accessToken: 'MOCK_ACCESS_TOKEN',
  refreshToken: 'MOCK_REFRESH_TOKEN',
  tokenType: 'Bearer',
  expiresIn: 1000000,
};

export const FIND_ACCOUNT_RESPONSE: FindAccountResponse = {
  email: 'example@email.com',
};

export const ACCESS_TOKEN_RESPONSE: AccessTokenResponse = {
  accessToken: 'MOCK_ACCESS_TOKEN',
  expiresIn: 1000000,
};
