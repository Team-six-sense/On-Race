// app/login/success/page.tsx
'use client';

import { useParams, useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { HiCheckCircle } from 'react-icons/hi2';

export default function LoginSuccess() {
  const params = useParams();
  const router = useRouter();

  return (
    <div className="flex flex-col items-center justify-center bg-white p-4">
      <div className="text-green-500">
        <HiCheckCircle size={80} />
      </div>
      <div className="text-3xl text-center p-4">
        <span className="font-bold">김유저</span>
        <span>
          님의 결제가 <br />
          정상적으로 완료되었습니다
        </span>
      </div>
      <div className="max-w-md w-full  space-y-6 p-4 rounded-sm border-1 border-gray-300 mb-4">
        <div className="flex justify-between items-center mb-4">
          <span className="text-xl text-black font-semibold">주문정보</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-base text-font-medium">주문번호</span>
          <span className="text-base font-medium">ORD20260215001</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-base text-font-medium">상품명</span>
          <span className="text-base font-medium">서울 마라톤 2026</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-base text-font-medium">결제일자</span>
          <span className="text-base font-medium">2026-02-15 14:30:25</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-base text-font-medium">결제수단</span>
          <span className="text-base font-medium">신한카드/일시불</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-base text-font-medium">결제 금액</span>
          <span className="text-base font-medium">51,000원</span>
        </div>
      </div>

      <div className="max-w-md w-full flex gap-2 items-center justify-center">
        <Button
          variant="secondary"
          rounded="full"
          size="lg"
          onClick={() => router.push('/')}
        >
          홈으로 이동하기
        </Button>

        <Button
          variant="primary1"
          rounded="full"
          size="lg"
          onClick={() => router.push(`/ticketing/${params.id}/payment/details`)}
        >
          결제 상세내역 보기
        </Button>
      </div>
    </div>
  );
}
