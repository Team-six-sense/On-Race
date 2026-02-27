// app/login/success/page.tsx
'use client';

import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';

export default function LoginSuccess() {
  const router = useRouter();

  return (
    <div className="flex flex-col items-center justify-center bg-white p-4">
      <div className="max-w-xl w-full  space-y-6 p-10 rounded-2xl border-3 border-gray-300 ">
        <div>
          <h2 className="text-2xl font-extrabold text-black">
            결제가 완료되었습니다.
          </h2>
        </div>

        <div className="bg-gray-50 rounded-none border-2 border-gray-300 p-4">
          <p className="text-sm text-gray-500">이름: 온러닝</p>
          <p className="text-sm text-gray-500">
            이메일(아이디): example@email.com
          </p>
        </div>

        <div className="space-y-3">
          <Button
            variant="outline"
            rounded="none"
            onClick={() => router.push('/')}
          >
            메인 페이지로 이동
          </Button>
        </div>
      </div>
    </div>
  );
}
