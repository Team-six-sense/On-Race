'use client';

import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { LuRefreshCcw } from 'react-icons/lu';
import { useState } from 'react';
import { authService } from '@/features/auth/services';

export default function LoginSuccess() {
  const router = useRouter();
  const [email, setEmail] = useState<string>('');

  const handleSendPasswordResetLink = async () => {
    try {
      const response = await authService.sendPasswordResetLink({ email });
      alert(response.message);
    } catch (error: any) {
      throw new Error(error);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center bg-white p-4">
      <div className="max-w-xl w-full space-y-4 p-10">
        <div>
          <h2 className="flex text-2xl font-bold text-black">
            비밀번호 재설정
          </h2>
        </div>

        <div>
          <h2 className="font-semifold">비밀번호 재설정 요청</h2>
          <div className="bg-gray-50 rounded-sm border border-gray-300 p-3">
            <p className="text-sm text-gray-500">
              가입하신 이메일 주소를 입력하시면 비밀번호 재설정 인증 메일을
              보내드립니다.
            </p>
          </div>
        </div>

        <div className="text-sm">이메일</div>
        <div className="flex items-center gap-2">
          <div className="flex-1">
            <Input
              placeholder="example@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div>
            <Button
              variant="primary1"
              rounded="full"
              size="sm"
              onClick={handleSendPasswordResetLink}
            >
              인증번호 발송
            </Button>
          </div>
        </div>

        <div className="flex items-center border border-gray-200 bg-gray-50 rounded-sm mb-4">
          <LuRefreshCcw size={25} className="m-4 text-gray-700" />
          <span className="text-gray-700">
            인증 코드 유효 시간: 15분
            <br />※ 인증 메일은 1분 간격, 하루 최대 5회까지 재발송할 수
            있습니다.
          </span>
        </div>

        <div className="flex gap-2">
          <Button
            variant="outline"
            rounded="sm"
            onClick={() => router.push('/login/email')}
          >
            취소
          </Button>
          <Button
            variant="primary1"
            rounded="sm"
            onClick={() => router.push('/find/account')}
          >
            아이디 찾기
          </Button>
        </div>
      </div>
    </div>
  );
}
