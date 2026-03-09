'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

import { useRouter } from 'next/navigation';

export default function SignupForm() {
  const router = useRouter();

  return (
    <div className="flex-1 flex flex-col items-center justify-center bg-primary">
      <div className="flex flex-col w-full max-w-xl p-8 ">
        <div className="text-2xl font-bold py-4">이메일 회원가입</div>
        <div className="text-lg font-bold py-4">기본 정보 입력</div>

        <p className="text-sm mb-1">이메일 *</p>
        <div className="flex gap-2 space-y-2 mb-2">
          <Input variant="primary" placeholder="example@gmail.com" />
          <Button
            variant="outline"
            size="fit"
            rounded="sm"
            className="border-gray-400 text-gray-500  "
          >
            중복확인
          </Button>
        </div>

        <div className="space-y-2 mb-2">
          <Input
            variant="primary"
            placeholder="010-1234-5678"
            label="휴대폰번호"
          />
          <Input variant="primary" placeholder="인증번호를 입력해주세요*" />
        </div>

        <div className="space-y-2 mb-2">
          <Input
            variant="primary"
            placeholder="비밀번호를 입력하세요"
            label="비밀번호 *"
          />
          <Input
            variant="primary"
            placeholder="비밀번호를 입력하세요"
            label="비밀번호 확인 *"
          />
        </div>

        <div className="flex space-y-2 gap-2 mb-2">
          <Button
            variant="outline"
            rounded="sm"
            className="border-gray-200"
            onClick={() => router.push('/login')}
          >
            취소
          </Button>
          <Button
            variant="primary1"
            rounded="sm"
            onClick={() => router.push('/signup/email-auth')}
          >
            다음
          </Button>
        </div>
      </div>
    </div>
  );
}
