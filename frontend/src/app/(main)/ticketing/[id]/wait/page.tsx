// app/login/success/page.tsx
'use client';

import { useParams, useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { IoEllipsisHorizontalCircleSharp } from 'react-icons/io5';

export default function LoginSuccess() {
  const params = useParams();
  const router = useRouter();

  return (
    <div className="flex flex-col items-center justify-center bg-white p-4">
      <div className="text-gray-300">
        <IoEllipsisHorizontalCircleSharp size={80} />
      </div>
      <div className="text-3xl text-center p-4">
        <p className="pb-2">
          <span className="font-bold">김유저</span>
          님의 주문을 확인하고 있습니다.
        </p>

        <p className="text-base text-font-medium pb-2">
          아래 계좌 정보로
          <span className="text-black">2026.02.28 (토) 23:59까지 </span>
          입금해 주세요.
          <br />
          기한 내 미입금 시 주문이 취소됩니다.
        </p>
      </div>
      <div className="max-w-md w-full  space-y-6 p-4 rounded-sm border-1 border-gray-300 mb-4">
        <div className="flex justify-between items-center mb-4">
          <span className="text-xl text-black font-semibold">
            입금 계좌 안내
          </span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-base text-font-medium">계좌번호</span>
          <span className="text-base font-medium">
            신한은행 110-1234-567890
          </span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-base text-font-medium">예금주</span>
          <span className="text-base font-medium">박대표㈜온레이스</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-base text-font-medium">입금 기한</span>
          <span className="text-base font-medium">
            2026.02.28 (토) 23:59까지
          </span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-base text-font-medium">보내실 금액</span>
          <span className="text-base font-medium">156,000원</span>
        </div>

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
          <span className="text-base text-font-medium">주문일자</span>
          <span className="text-base font-medium">2026-02-15 14:30:25</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-base text-font-medium">결제수단</span>
          <span className="text-base font-medium">무통장입금/김유저</span>
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
          주문 상세내역 보기
        </Button>
      </div>
    </div>
  );
}
