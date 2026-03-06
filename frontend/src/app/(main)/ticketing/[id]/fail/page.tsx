// app/login/success/page.tsx
'use client';

import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { HiMiniXCircle } from 'react-icons/hi2';

export default function LoginSuccess() {
  const router = useRouter();

  return (
    <div className="flex flex-col items-center justify-center bg-white p-4">
      <div className="text-red-600 py-6">
        <HiMiniXCircle size={60} />
      </div>
      <div className="text-2xl text-center p-4">
        <span className="font-bold">결제에 실패했습니다</span>
      </div>
      <div className="text-lg text-center p-4">
        <span className="text-gray-500">
          결제가 정상적으로 완료되지 않아 참가 신청이 확정되지 않았습니다.
          <br />
          결제 정보를 확인한 후 다시 시도해 주세요.
        </span>
      </div>

      <div className="max-w-md w-full flex gap-2 items-center justify-center">
        <Button
          variant="outline"
          rounded="full"
          size="lg"
          onClick={() => router.push('/')}
        >
          메인 페이지로 이동
        </Button>

        <Button
          variant="primary1"
          rounded="full"
          size="lg"
          onClick={() => router.push('/')}
        >
          주문내역 보기
        </Button>
      </div>
    </div>
  );
}
