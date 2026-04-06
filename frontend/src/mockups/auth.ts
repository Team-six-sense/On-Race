import {
  AccessTokenResponse,
  FindAccountResponse,
  LoginResponse,
  SignupResponse,
  Term,
  TermDetails,
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

export const MOCK_TERMS: Term[] = [
  {
    termVersionId: 1,
    termName: '이용약관',
    required: true,
    version: '1.0',
  },
  {
    termVersionId: 2,
    termName: '개인정보처리방침',
    required: true,
    version: '1.0',
  },
  {
    termVersionId: 3,
    termName: '마케팅 수신 동의',
    required: false,
    version: '1.0',
  },
];

export const MOCK_TERM_DETAILS: TermDetails[] = [
  {
    termVersionId: 1,
    termName: '이용약관',
    required: true,
    version: '1.0',
    content:
      '본 약관은 On-Race 서비스의 이용 조건 및 절차에 관한 사항을 규정합니다.',
  },
  {
    termVersionId: 2,
    termName: '개인정보처리방침',
    required: true,
    version: '1.0',
    content:
      '수집하는 개인정보의 항목, 수집 및 이용목적, 보유 및 이용기간 등을 안내합니다.',
  },
  {
    termVersionId: 3,
    termName: '마케팅 수신 동의',
    required: false,
    version: '1.0',
    content:
      '이벤트, 혜택 등 마케팅 정보를 이메일 및 SMS로 수신하는 것에 동의합니다.',
  },
];
