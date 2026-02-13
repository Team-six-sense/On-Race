'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { SocialLoginButtons } from '@/features/auth/components';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const router = useRouter();
  return (
    <div className="mt-40 flex flex-col items-center justify-center bg-primary">
      <div className="p-8 rounded-2xl w-full max-w-md flex flex-col">
        <h1>LOGO</h1>
        <h1 className="text-2xl font-bold mb-2">로그인 또는 계정 만들기</h1>
        <p className="text-gray-500 mb-8">
          로그인하려면 이메일과 패스워드를 입력해주세요
        </p>

        <Input className="my-2" placeholder="이메일*" />
        <Input className="my-2" placeholder="비밀번호*" />
        <Button
          className="my-2"
          rounded="full"
          onClick={() => router.push('/')}
        >
          로그인하기
        </Button>

        <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-4 w-full">
          <div className="h-[1px] bg-gray-300"></div>
          <span className="text-gray-600">또는</span>
          <div className="h-[1px] bg-gray-300"></div>
        </div>

        {/* 구글 로그인 버튼 */}
        <SocialLoginButtons />

        <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-1 w-full">
          <Button variant="link">이메일 찾기</Button>
          <Button variant="link">비밀번호 찾기</Button>
          <Button variant="link">회원가입</Button>
        </div>
      </div>
    </div>
  );
}
