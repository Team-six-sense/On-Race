'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { SocialLoginButtons } from '@/features/auth/components';
import { useRouter } from 'next/navigation';

export default function EmailLoginPage() {
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
          rounded="full"
          onClick={() => router.push('/')}
        >
          로그인 하기
        </Button>

        <div className="flex justify-end items-center w-full">
          <Button className="px-1 text-gray-400" variant="text" size="fit">
            아이디 찾기
          </Button>

          <span className="text-sm text-gray-400">|</span>

          <Button className="px-1 text-gray-400" variant="text" size="fit">
            비밀번호 재설정
          </Button>

          <span className="text-sm text-gray-400">|</span>

          <Button
            className="px-1 text-gray-400"
            variant="text"
            size="fit"
            onClick={() => router.push('/signup/agree')}
          >
            회원가입
          </Button>
        </div>
      </div>
    </div>
  );
}
