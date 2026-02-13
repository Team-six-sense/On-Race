import { NextAuthOptions } from 'next-auth';
import GoogleProvider from 'next-auth/providers/google';
import NaverProvider from 'next-auth/providers/naver';
import KakaoProvider from 'next-auth/providers/kakao';

export const authOptions: NextAuthOptions = {
  providers: [
    GoogleProvider({
      clientId: process.env.GOOGLE_CLIENT_ID!,
      clientSecret: process.env.GOOGLE_CLIENT_SECRET!,
      authorization: {
        params: {
          prompt: 'select_account', // 매번 계정 선택창을 띄움
          // access_type: "offline",   // (선택사항) 리프레시 토큰이 필요할 경우
          response_type: 'code',
        },
      },
    }),
    NaverProvider({
      clientId: process.env.NAVER_CLIENT_ID!,
      clientSecret: process.env.NAVER_CLIENT_SECRET!,
      authorization: {
        params: {
          auth_type: 'reauthenticate', // 재인증을 강제하여 계정 선택/로그인창 유도
        },
      },

      profile(profile) {
        return {
          id: profile.response.id,
          name: profile.response.name || profile.response.nickname, // 이름이 없으면 별명이라도 가져옴
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
          // 'login': 매번 카카오 계정 로그인 창(ID/PW 입력)을 띄움
          // 'select_account': 이미 로그인된 계정 리스트 중 선택하거나 다른 계정으로 로그인 유도
          prompt: 'login',
        },
      },
    }),
  ],
  pages: {
    signIn: '/login', // 커스텀 로그인 페이지 경로
  },
  callbacks: {
    async jwt({ token, account, profile }) {
      // 로그인 시점에 실행: 유저 정보를 토큰에 저장
      if (account && profile) {
        token.accessToken = account.access_token;
      }
      return token;
    },
    async session({ session, token }) {
      // 클라이언트에서 세션을 사용할 때 실행
      return session;
    },
  },
};
