'use client';

import { useParams, useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { HiCheckCircle } from 'react-icons/hi2';
import { useEffect, useState } from 'react';
import { formatKoreanDate } from '@/features/ticketing/utils/date';

interface PaymentData {
  orderName: string;
  method: string;
  approvedAt: string;
}

export default function LoginSuccess() {
  const params = useParams();
  const router = useRouter();

  const searchParams = useSearchParams();
  const [isConfirmed, setIsConfirmed] = useState(false);
  const [data, setData] = useState<PaymentData>();

  const paymentKey = searchParams.get('paymentKey');
  const orderId = searchParams.get('orderId');
  const amount = searchParams.get('amount');

  useEffect(() => {
    // 백엔드(Route Handler)로 승인 요청을 보냄
    async function confirmPayment() {
      const response = await fetch('/api/payment/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          paymentKey,
          orderId,
          amount,
        }),
      })
        .then((res) => res.json())
        .then((data) => {
          setData(data);
          setIsConfirmed(true);
        })
        .catch((err) => {
          console.error('데이터 로드 실패:', err);
          setIsConfirmed(true);
        });
    }

    if (paymentKey && orderId && amount) {
      confirmPayment();
    }
  }, [paymentKey, orderId, amount]);

  return (
    <div className="flex flex-col items-center justify-center bg-white p-4">
      {isConfirmed && data ? (
        <>
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
              <span className="text-base font-medium">{orderId}</span>
            </div>
            <div className="flex justify-between items-center mb-4">
              <span className="text-base text-font-medium">상품명</span>
              <span className="text-base font-medium">{data?.orderName}</span>
            </div>
            <div className="flex justify-between items-center mb-4">
              <span className="text-base text-font-medium">결제일자</span>
              <span className="text-base font-medium">
                {formatKoreanDate(data?.approvedAt, true)}
              </span>
            </div>
            <div className="flex justify-between items-center mb-4">
              <span className="text-base text-font-medium">결제수단</span>
              <span className="text-base font-medium">{data?.method}</span>
            </div>
            <div className="flex justify-between items-center mb-4">
              <span className="text-base text-font-medium">결제 금액</span>
              <span className="text-base font-medium">
                {Number(amount).toLocaleString()}원
              </span>
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
              onClick={() =>
                router.push(`/ticketing/${params.id}/payment/details`)
              }
            >
              결제 상세내역 보기
            </Button>
          </div>
        </>
      ) : (
        <p>결제 승인 중...</p>
      )}
    </div>
  );
}
