export interface SocialLoginRequest {
  email: string;
  name: string;
  provider: 'google' | 'kakao' | 'naver';
  accessToken: string; // 소셜 서버에서 받은 토큰
}

export interface LoginResponse {
  accessToken: string; // Spring에서 발급한 JWT
  refreshToken: string;
  user: {
    id: number;
    email: string;
    name: string;
    role: string;
  };
}
