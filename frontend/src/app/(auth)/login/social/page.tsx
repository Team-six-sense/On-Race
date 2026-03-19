'use client';

import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

export default function SocialLoginPage() {
  const { data: session, status } = useSession();
  const router = useRouter();

  useEffect(() => {
    // 인증이 완료될 때까지 대기
    if (status === 'loading') return;

    // 인증 성공 시 분기 처리
    if (status === 'authenticated' && session) {
      const isSignup = true;

      console.log(JSON.stringify(session, null, 2));

      if (isSignup) {
        // 신규 가입자라면 (예: 추가 정보 입력 페이지)
        router.replace('/signup/social/agree');
      } else {
        // 기존 회원이라면 (예: 메인 대시보드)
        router.replace('/');
      }
    }

    // 인증 실패 시 (로그아웃 상태 등)
    if (status === 'unauthenticated') {
      router.replace('/login');
    }
  }, [status, session, router]);

  // 리다이렉트 되는 동안 보여줄 화면 (스피너 등)
  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mb-4"></div>
      <p className="text-lg font-medium text-gray-600">
        사용자 정보를 확인 중입니다...
      </p>
    </div>
  );
}
