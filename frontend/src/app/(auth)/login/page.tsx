'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { SocialLoginButtons } from '@/features/auth/components';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const router = useRouter();
  return (
    <div className="flex-1 flex flex-col items-center justify-center bg-primary">
      <div className="flex flex-col w-full max-w-md p-8 ">
        <h1 className="my-2 text-xl font-bold">이메일 로그인</h1>

        <Input
          className="my-2"
          variant="primary"
          placeholder="example@gmail.com"
          label="이메일 *"
        />
        <Input
          className="my-2"
          variant="primary"
          placeholder="비밀번호를 입력해주세요"
          label="비밀번호 *"
        />
        <Button
          className="my-2"
          rounded="none"
          onClick={() => router.push('/')}
        >
          로그인
        </Button>

        <div className="flex justify-end items-center w-full">
          <Button className="pr-1 text-blue-600" variant="text" size="fit">
            아이디 찾기
          </Button>
          <Button className="pr-1 text-blue-600" variant="text" size="fit">
            비밀번호 재설정
          </Button>
        </div>

        <div className="my-2 h-[1px] bg-gray-300"></div>

        <h1 className="mt-2 text-xl font-bold">SNS 로그인</h1>
        <SocialLoginButtons />

        <div className="mt-2 h-[1px] bg-gray-300"></div>

        <div className="flex items-center justify-center">
          <span className="text-gray-700">아직도 회원이 아니신가요? </span>
          <Button
            className="pr-1 text-blue-600"
            variant="text"
            size="fit"
            onClick={() => router.push('/signup')}
          >
            회원가입
          </Button>
        </div>
      </div>
    </div>
  );
}
