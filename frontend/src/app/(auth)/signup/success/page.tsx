'use client';

import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { useSignupStore } from '@/features/auth/store/useSignupStore';

export default function LoginSuccess() {
  const router = useRouter();
  const { email, resetSignupData } = useSignupStore();

  const handleFinalSignup = () => {
    resetSignupData();
    router.push('/login/email');
  };

  return (
    <div className="flex flex-col items-center justify-center bg-white p-4">
      <div className="max-w-xl w-full space-y-4 p-10">
        <div>
          <h2 className="flex text-2xl font-bold text-black">회원가입 완료</h2>
        </div>
        <div>
          <h2 className="font-semifold">회원가입이 완료되었습니다.</h2>
        </div>

        <div className="bg-gray-50 rounded-none border-2 border-gray-300 p-2">
          <p className="text-sm text-gray-500">이름: 온러닝</p>
          <p className="text-sm text-gray-500">이메일(아이디): {email}</p>
        </div>

        <div>
          <Button variant="primary1" rounded="sm" onClick={handleFinalSignup}>
            로그인 페이지로 이동
          </Button>
        </div>
      </div>
    </div>
  );
}
