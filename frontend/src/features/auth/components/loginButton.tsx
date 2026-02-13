'use client';

import { useRouter } from 'next/navigation';
import { signIn } from 'next-auth/react';
import { RiKakaoTalkFill } from 'react-icons/ri';
import { SiNaver } from 'react-icons/si';
import { FcGoogle } from 'react-icons/fc';
import { Button } from '@/components/ui/button';

export function SocialLoginButtons() {
  const router = useRouter();

  const handleLogin = (provider: 'google' | 'kakao' | 'naver') => {
    signIn(provider, { callbackUrl: '/' });
  };
  return (
    <div className="py-5 flex flex-col justify-center gap-3 w-full max-w-sm">
      {/* 구글 로그인: White/Border */}
      <Button
        rounded="none"
        onClick={() => handleLogin('google')}
        className="gap-3 bg-white text-[#3c4043] border border-gray-300 hover:bg-gray-50"
      >
        <FcGoogle className="w-5 h-5 flex-shrink-0" />
        구글 로그인
      </Button>

      {/* 카카오 로그인: #FEE500 */}
      <Button
        rounded="none"
        onClick={() => handleLogin('kakao')}
        className="gap-3 bg-[#FEE500] text-[#191919] hover:bg-[#FADA0A]"
      >
        <RiKakaoTalkFill className="w-5 h-5 " />
        카카오 로그인
      </Button>

      {/* 네이버 로그인: #03C75A */}
      <Button
        rounded="none"
        onClick={() => handleLogin('naver')}
        className="gap-3 bg-[#03C75A] text-white hover:bg-[#02b350]"
      >
        <SiNaver className="w-5 h-5 flex-shrink-0" />
        네이버 로그인
      </Button>
    </div>
  );
}
