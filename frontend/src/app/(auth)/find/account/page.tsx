'use client';

import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { authService } from '@/features/auth/services';
import { useState } from 'react';
import { LuPhone } from 'react-icons/lu';

export default function LoginSuccess() {
  const router = useRouter();
  const [phoneNumber, setPhoneNumber] = useState<string>('');

  const handleFindAccount = async () => {
    try {
      const response = await authService.findAccount({ phoneNumber });

      if (response.success) {
        router.push('/find/account/result');
      }
    } catch (error) {
      console.error('데이터 로드 실패:', error);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center bg-white p-4">
      <div className="max-w-xl w-full space-y-4">
        <div>
          <h2 className="flex text-4xl font-bold text-black pb-6">
            아이디 찾기
          </h2>
        </div>

        <div>
          <h2 className="text-2xl font-bold">전화번호를 통한 아이디 찾기</h2>

          <div className="flex items-center border border-gray-100 bg-gray-50 rounded-sm mb-4">
            <LuPhone size={30} className="m-4 text-font-medium" />
            <span className="text-base text-font-medium">
              전화번호를 입력하시면 가입된 이메일 정보를 확인하실 수 있습니다.
            </span>
          </div>
        </div>

        <div className="mb-6">
          <Input
            label="휴대폰번호"
            placeholder="010-1234-5678"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
          />
        </div>

        <div className="flex gap-2">
          <Button
            variant="outline"
            rounded="sm"
            onClick={() => router.push('/find/password')}
          >
            비밀번호 재설정
          </Button>
          <Button variant="primary1" rounded="sm" onClick={handleFindAccount}>
            찾기
          </Button>
        </div>
      </div>
    </div>
  );
}
