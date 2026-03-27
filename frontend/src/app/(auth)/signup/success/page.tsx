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
    <div className="flex items-center justify-center bg-white p-4">
      <div className="max-w-xl w-full p-10">
        <div>
          <div className="flex text-4xl font-bold text-black mb-8">
            회원가입 완료
          </div>
        </div>
        <div>
          <div className="text-2xl font-medium mb-2">
            회원가입이 완료되었습니다.
          </div>
        </div>

        <div className="bg-gray-200 rounded-sm p-4 mb-6 space-y-2">
          <div className="flex items-center">
            <p className="w-32 text-base text-black">이름</p>
            <p className="text-base font-font-medium">온러닝</p>
          </div>
          <div className="flex items-center">
            <p className="w-32 text-base text-black">이메일(아이디)</p>
            <p className="text-base font-font-medium">{email}</p>
          </div>
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
