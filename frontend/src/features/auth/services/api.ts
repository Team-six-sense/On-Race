import axios from 'axios';
import { LoginResponse, SocialLoginRequest } from '../types';

export const authService = {
  // 소셜 로그인 정보를 Spring 백엔드에 전달하고 JWT를 받아옴
  async loginWithSocial(data: SocialLoginRequest): Promise<LoginResponse> {
    // 백엔드가 완성되면 BASE_URL을 실제 Spring 서버로 변경
    const response = await axios.post<LoginResponse>(
      '/api/mock/auth/login',
      data,
    );
    return response.data;
  },
};
