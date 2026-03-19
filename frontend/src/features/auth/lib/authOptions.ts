import { NextAuthOptions } from 'next-auth';
import CredentialsProvider from 'next-auth/providers/credentials';
import GoogleProvider from 'next-auth/providers/google';
import NaverProvider from 'next-auth/providers/naver';
import KakaoProvider from 'next-auth/providers/kakao';
import { authService } from '../services';
import { LoginRequest } from '../types';

export const authOptions: NextAuthOptions = {
  providers: [
    CredentialsProvider({
      name: 'Email Login',
      credentials: {
        email: { label: 'Email', type: 'text' },
        password: { label: 'Password', type: 'password' },
      },
      async authorize(credentials) {
        try {
          const data: LoginRequest = {
            email: credentials?.email?.toString() ?? '',
            password: credentials?.password?.toString() ?? '',
          };

          // 백엔드 API 호출
          const response = await authService.login(data);

          // 응답 성공 여부 확인
          if (response.success && response.data) {
            const user = {
              id: response.data.id.toString(),
              email: response.data.email || credentials?.email,
              name: response.data.name || credentials?.email,
              accessToken: response.data.accessToken,
              refreshToken: response.data.refreshToken,
            };

            return user;
          }

          return null;
        } catch (error: any) {
          console.error('Login Authorize Error:', error);

          return null;
        }
      },
    }),
    GoogleProvider({
      clientId: process.env.GOOGLE_CLIENT_ID!,
      clientSecret: process.env.GOOGLE_CLIENT_SECRET!,
      authorization: {
        params: {
          prompt: 'select_account',
          response_type: 'code',
        },
      },
    }),
    NaverProvider({
      clientId: process.env.NAVER_CLIENT_ID!,
      clientSecret: process.env.NAVER_CLIENT_SECRET!,
      authorization: {
        params: {
          auth_type: 'reauthenticate',
        },
      },
      profile(profile) {
        return {
          id: profile.response.id,
          name: profile.response.name || profile.response.nickname,
          email: profile.response.email,
          image: profile.response.profile_image,
        };
      },
    }),
    KakaoProvider({
      clientId: process.env.KAKAO_CLIENT_ID!,
      clientSecret: process.env.KAKAO_CLIENT_SECRET!,
      authorization: {
        params: {
          prompt: 'login',
        },
      },
    }),
  ],
  callbacks: {
    async jwt({ token, account, user }) {
      // 일반 로그인 (Credentials) 시 실행
      if (account && user) {
        if (account.provider === 'credentials') {
          // 로컬 로그인 유저 전용 데이터 추가
          token.loginType = 'local';
          token.springAccessToken = user.accessToken;
          token.springRefreshToken = user.refreshToken;
        } else {
          // 소셜 로그인 유저 전용 데이터 추가
          token.loginType = 'social';
          const data: LoginRequest = {
            email: user.email ?? '',
            password: `social_login_${user.id}`,
          };

          const response = await authService.login(data);

          // 응답 성공 여부 확인
          if (response.success && response.data) {
            const user = {
              id: response.data.id.toString(),
              email: response.data.email,
              name: response.data.name,
              accessToken: response.data.accessToken,
              refreshToken: response.data.refreshToken,
            };

            token.springAccessToken = user.accessToken;
            token.springRefreshToken = user.refreshToken;
          }

          //  try {
          //     const response = await fetch(
          //       `${process.env.SPRING_API_URL}/api/v1/auth/social-login`,
          //       {
          //         method: 'POST',
          //         headers: { 'Content-Type': 'application/json' },
          //         body: JSON.stringify({
          //           type: 'social',
          //           provider: account.provider,
          //           accessToken: account.access_token,
          //           email: user.email,
          //           name: user.name,
          //         }),
          //       },
          //     );

          //     const data = await response.json();
          //     if (response.ok) {
          //       token.springAccessToken = data.accessToken;
          //     }
          //   } catch (error) {
          //     console.error('Social Login Sync Error:', error);
          //   }
        }
      }

      return token;
    },

    async session({ session, token }) {
      // 공통으로 Spring 토큰을 세션에 주입

      session.accessToken = token.springAccessToken;
      session.refreshToken = token.springRefreshToken;

      console.log('session', session);
      return session;
    },
  },
};
