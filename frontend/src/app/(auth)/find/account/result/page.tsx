// app/login/success/page.tsx
'use client';

import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

export default function LoginSuccess() {
  const router = useRouter();

  return (
    <div className="flex flex-col items-center justify-center bg-white p-4">
      <div className="max-w-xl w-full space-y-4 p-10">
        <div>
          <h2 className="flex text-2xl font-bold text-black">아이디 찾기</h2>
        </div>

        <div>
          <h2 className="font-semifold">전화번호를 통한 아이디 찾기</h2>
          <div className="bg-gray-50 rounded-sm border border-gray-300 p-3">
            <p className="text-sm text-gray-500">
              가입한 이메일: exam***@***.com
            </p>
            <p className="text-sm text-gray-500">가입일: 2026-01-15</p>
          </div>
        </div>

        <div className="flex gap-2">
          <Button
            variant="outline"
            rounded="sm"
            onClick={() => router.push('/find/password')}
          >
            비밀번호 재설정
          </Button>
          <Button
            variant="primary1"
            rounded="sm"
            onClick={() => router.push('/login/email')}
          >
            로그인 페이지로 이동
          </Button>
        </div>
      </div>
    </div>
  );
}
