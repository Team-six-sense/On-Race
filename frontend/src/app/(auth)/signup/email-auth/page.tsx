'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

import { useRouter } from 'next/navigation';
import { LuMail, LuRefreshCcw } from 'react-icons/lu';

export default function SignupForm() {
  const router = useRouter();

  return (
    <div className="flex-1 flex flex-col items-center justify-center bg-primary">
      <div className="flex flex-col w-full max-w-xl p-8 ">
        <div className="text-2xl font-bold py-4">이메일 회원가입</div>
        <div className="text-lg font-bold py-4">이메일 인증</div>

        <div className="flex items-center border border-gray-200 bg-gray-50 rounded-sm mb-4">
          <LuMail size={30} className="m-4 text-gray-700" />
          <span className="text-gray-700">
            example@email.com으로 인증 번호가 발송되었습니다.
            <br />
            메일함을 확인하시고 인증 번호를 입력해주세요.
          </span>
        </div>

        <div className="space-y-2 mb-4">
          <Input variant="primary" placeholder="1234" label="인증번호 *" />
          <p className="text-xs text-gray-400 px-2">
            이메일로 전송된 4자리 인증 코드를 입력해주세요
          </p>
        </div>

        <div className="flex items-center border border-gray-200 bg-gray-50 rounded-sm mb-4">
          <LuRefreshCcw size={30} className="m-4 text-gray-700" />
          <span className="text-gray-700">
            인증 코드 유효 시간: 15분
            <br />※ 인증 메일은 1분 간격, 하루 최대 5회까지 재발송할 수
            있습니다.
          </span>
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
            onClick={() => router.push('/signup/success')}
          >
            다음
          </Button>
        </div>
      </div>
    </div>
  );
}
