'use client';

import React, { useEffect, useState } from 'react';

import { Button } from '@/components/ui/button';
import { useEventStore } from '@/features/event/store/useEventStore';
import { format, parseISO } from 'date-fns';
import { useRouter, useSearchParams } from 'next/navigation';
import { cn } from '@/lib/utils';
import { formatKoreanDate } from '@/features/ticketing/utils/date';

export default function CheckoutPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const {
    event,
    eventDetails,
    course,
    pace,
    eventSaleInfo,
    selectedBasicOption,
    selectedOptions,
  } = useEventStore();
  const [paymentData, setPaymentData] = useState<any>();

  const paymentKey = searchParams.get('paymentKey');
  const orderId = searchParams.get('orderId');

  const PAYMENT_STATUS_LABEL: Record<string, string> = {
    READY: '결제 생성',
    IN_PROGRESS: '결제 진행 중',
    WAITING_FOR_DEPOSIT: '입금 대기',
    DONE: '결제 완료',
    CANCELED: '결제 취소',
    PARTIAL_CANCELED: '부분 취소',
    ABORTED: '결제 실패',
    EXPIRED: '만료됨',
  };

  const basicOptionItem: Record<string, string> = {
    option1: 'S(90~95)',
    option2: 'M(95~100)',
    option3: 'L(100~105)',
    option4: 'XL(105~120)',
  };

  const optionItem = [
    { value: 'option1', label: '선택안함', price: 0 },
    { value: 'option2', label: '텀블러', price: 15000 },
    { value: 'option3', label: '스포츠타올', price: 12000 },
    { value: 'option4', label: '러닝화', price: 89000 },
  ];

  const cardNames: Record<string, string> = {
    '31': '삼성',
    '61': '현대',
    '41': '신한',
    '11': 'KB국민',
    '51': '롯데',
    '71': '하나',
    '21': '비씨',
    '91': 'NH농협',
    '15': '카카오뱅크',
    '24': '토스뱅크',
  };

  const price = eventDetails?.courses?.[0]?.price ?? 0;
  const discountRate = event?.discountRate ?? 0;
  const discountPrice = price * (discountRate / 100);

  const deliveryFee =
    eventSaleInfo?.delivery.deliveryFee === '무료'
      ? 0
      : Number(eventSaleInfo?.delivery.deliveryFee);

  const optionPrice = selectedOptions.reduce((sum, id) => {
    const item = optionItem.find((opt) => opt.value === id);
    return sum + (item ? Number(item.price) : 0);
  }, 0);

  const totalPrice = price - discountPrice + optionPrice;
  const paymentPrice = totalPrice - deliveryFee;

  let approvedAt = '날짜 정보 없음';

  if (paymentData?.approvedAt) {
    approvedAt = format(
      parseISO(paymentData?.approvedAt),
      'yyyy-MM-dd HH:mm:ss',
    );
  }

  useEffect(() => {
    async function fetchPaymentDetails() {
      // 직접 토스 API가 아니라, '내가 만든 API'를 호출합니다.
      const response = await fetch(
        `/api/payment/details?paymentKey=${paymentKey}`,
      );
      const detailData = await response.json();
      setPaymentData(detailData);
    }

    if (paymentKey) {
      fetchPaymentDetails();
    }
  }, [paymentKey]);

  // 공통 섹션 컴포넌트 (좌측 헤더 | 우측 내용)
  const FormSection = ({
    title,
    children,
  }: {
    title: string;
    children: React.ReactNode;
  }) => (
    <div className="flex flex-col md:flex-row border-b border-gray-100 py-10 last:border-0">
      <div className="w-full md:w-1/5 mb-4 md:mb-0 flex items-center gap-2 self-start">
        <h2 className="text-lg font-bold text-gray-800">{title}</h2>
      </div>
      <div className="w-full md:w-4/5">{children}</div>
    </div>
  );

  const discountParticipationFee = () => {
    const ratio = 1 - discountRate / 100;
    return (price * ratio).toLocaleString();
  };

  return (
    <div className="bg-white max-w-7xl mx-auto px-30 py-4">
      <div className="">
        <header className="mb-4">
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
            결제 상세내역
          </h1>
          <div className="my-2 h-[2px] bg-black"></div>
        </header>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-16">
          {/* 왼쪽 영역: 입력 폼 */}
          <div className="lg:col-span-8">
            <FormSection title="주문현황">
              <div className="flex flex-col gap-4">
                <div className="flex-1 space-y-2">
                  <div className="flex">
                    <span className="w-28 text-gray-600">주문번호</span>
                    <span className="flex-1">{orderId}</span>
                  </div>
                  <div className="flex">
                    <span className="w-28 text-gray-600">주문일시</span>
                    <span className="flex-1">{approvedAt}</span>
                  </div>
                  <div className="flex items-center">
                    <span className="w-28 text-gray-600">주문상태</span>
                    <span className="flex-1 font-semibold">
                      {PAYMENT_STATUS_LABEL[paymentData?.status]}
                    </span>
                    <Button variant="outline" size="fit">
                      결제취소
                    </Button>
                  </div>
                  <div className="flex items-center">
                    <span className="w-28 text-gray-600">배송현황</span>
                    <span className="flex-1"> - </span>
                    <Button variant="outline" size="fit" disabled={true}>
                      배송조회
                    </Button>
                  </div>
                </div>
              </div>
            </FormSection>

            {/* 주문 상품 정보 */}
            <FormSection title="주문 상품">
              <div className="flex flex-col gap-4">
                <div className="flex gap-6">
                  <div className="w-30 h-30 flex-shrink-0">
                    <img
                      src={'/image/default.png'}
                      className="w-full h-full object-cover rounded-sm"
                    />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-black">
                      {event?.title}
                    </h3>
                    <div className="flex flex-col text-sm ">
                      <div className="flex items-center text-sm text-font-low font-medium py-1">
                        <span>
                          {formatKoreanDate(event?.eventAt ?? '', true)}
                        </span>
                        <span className="px-1">•</span>
                        <span>{event?.venue}</span>
                      </div>
                      <div className="flex items-center">
                        <span
                          className={cn(
                            'text-base mr-1',
                            discountRate > 0
                              ? 'text-sm text-font-low line-through'
                              : 'text-black',
                          )}
                        >
                          {price.toLocaleString()}원
                        </span>

                        {discountRate > 0 && (
                          <span className="flex items-center text-base font-semibold">
                            {discountParticipationFee()}원
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                  <Button
                    variant="outline"
                    size="fit"
                    onClick={() => router.push(`/ticketing/${event?.id}`)}
                  >
                    상세보기
                  </Button>
                </div>

                <div className="flex-1 space-y-2">
                  <div className="flex">
                    <span className="w-28 text-gray-600">코스</span>
                    <span className="flex-1">{course}</span>
                  </div>
                  <div className="flex">
                    <span className="w-28 text-gray-600">페이스</span>
                    <span className="flex-1">{pace}</span>
                  </div>
                  <div className="flex text-base">
                    <span className="w-28 text-gray-600 flex items-start">
                      기본 옵션1
                    </span>
                    <div className="flex-1">
                      <div>
                        <span>기념 티셔츠</span>
                        <span className="text-gray-600"> | </span>
                        <span className="text-gray-600">
                          {basicOptionItem[selectedBasicOption]}
                        </span>
                      </div>
                      <div>
                        <span className="texdt-lg font-semibold">무료</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex text-base">
                    <span className="w-28 text-gray-600 flex items-center">
                      기본 옵션2
                    </span>
                    <div className="flex-1">
                      <span>완주 메달</span>
                      <span className="text-gray-600"> | </span>
                      <span className="text-gray-600">
                        완주자에 한해 현장 증정
                      </span>
                    </div>
                  </div>
                  <div className="flex text-base font-medium">
                    {selectedOptions.length > 0 && (
                      <div className="flex">
                        <span className="w-28 text-gray-600 flex items-start">
                          선택 옵션
                        </span>
                        <div className="flex-1 flex flex-col gap-3">
                          {selectedOptions.map((val) => {
                            const item = optionItem.find(
                              (opt) => opt.value === val,
                            );

                            if (!item) return null;

                            return (
                              <div key={item.value}>
                                <div>
                                  <span>{item.label}</span>
                                </div>
                                <div>
                                  <span className="font-semibold">
                                    {item.price.toLocaleString()}원
                                  </span>
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </FormSection>

            {/* 참가자 정보 */}
            <FormSection title="참가자 정보">
              <div className="flex flex-col gap-4">
                <div className="flex-1 space-y-2">
                  <div className="flex text-base">
                    <span className="w-28 text-gray-600">이름</span>
                    <span className="flex-1">김유저</span>
                  </div>
                  <div className="flex">
                    <span className="w-28 text-gray-600">성별</span>
                    <span className="flex-1">여</span>
                  </div>
                  <div className="flex items-center">
                    <span className="w-28 text-gray-600">생년월일</span>
                    <span className="flex-1">1999년 1월 1일</span>
                  </div>
                  <div className="flex items-center">
                    <span className="w-28 text-gray-600">휴대폰번호</span>
                    <span className="flex-1">010-1234-5678</span>
                  </div>
                  <div className="flex items-center">
                    <span className="w-28 text-gray-600">이메일</span>
                    <span className="flex-1">user@email.com</span>
                  </div>
                </div>
              </div>
            </FormSection>

            {/* 배송지 정보 */}
            <FormSection title="배송지 정보">
              <div className="flex justify-between items-start">
                <div className="flex flex-wrap gap-2 items-center">
                  <span className="font-bold text-gray-900">우리 집</span>
                  <div className="flex gap-1.5">
                    <span className="text-[10px] px-2 py-1 bg-gray-100 text-gray-600 font-bold rounded-sm">
                      기본배송지
                    </span>
                  </div>
                </div>
                <div className="flex">
                  <Button variant="outline" size="sm" rounded="sm">
                    변경하기
                  </Button>
                </div>
              </div>

              {/* 정보 섹션 */}
              <div className="space-y-1">
                {/* 주소 */}
                <div className="flex gap-1">
                  <div className="text-base font-semibold">
                    서울특별시 강남구 테헤란로 123, 좋은아파트 102동 304호
                  </div>
                </div>

                {/* 연락처 */}
                <div className="flex items-center gap-3">
                  <span className="text-sm text-gray-700">
                    김유저 - 010-1234-5678
                  </span>
                </div>

                {/* 배송 요청사항  */}
                <div className="pt-3">
                  <div className="flex items-center gap-3">
                    <div>
                      <p className="text-base text-gray-500">배송요청사항</p>
                    </div>
                    <div className="flex-1">
                      <span className="text-sm font-semibold">
                        문 앞에 두고 가주세요
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </FormSection>

            {/* 결제 수단 */}
            <FormSection title="결제 수단">
              {/* 카드 결제 시 */}
              {paymentData?.card && (
                <div className="flex flex-col gap-2">
                  <div>
                    <span className="text-lg font-semibold">카드 결제</span>
                  </div>
                  <div className="flex text-base">
                    <span className="w-28 text-gray-600">카드사</span>
                    <span className="flex-1 font-medium">
                      {cardNames[paymentData.card.issuerCode] || '기타카드'}
                    </span>
                  </div>
                  <div className="flex text-base">
                    <span className="w-28 text-gray-600">카드번호</span>
                    <span className="flex-1 font-medium">
                      {paymentData.card.number}
                    </span>
                  </div>
                  <div className="flex text-base">
                    <span className="w-28 text-gray-600">할부여부</span>
                    <span className="flex-1 font-medium">
                      {paymentData.card.installmentPlanMonths === 0
                        ? '일시불'
                        : `${paymentData.card.installmentPlanMonths}개월`}
                    </span>
                  </div>
                </div>
              )}

              {/* 간편결제 (카카오페이, 토스페이 등) 시 */}
              {paymentData?.easyPay && (
                <div>
                  <span className="text-lg font-semibold">
                    간편결제 ({paymentData.easyPay.provider})
                  </span>
                </div>
              )}

              {/* 휴대폰 결제 시 */}
              {paymentData?.mobilePhone && (
                <>
                  <div>
                    <span>결제수단</span>
                    <span>휴대폰 결제</span>
                  </div>
                  <div>
                    <span>통신사</span>
                    <span>{paymentData.mobilePhone.carrier}</span>
                  </div>
                  <div>
                    <span>휴대폰번호</span>
                    <span>{paymentData.mobilePhone.customerMobilePhone}</span>
                  </div>
                </>
              )}
            </FormSection>
          </div>

          {/* 오른쪽 영역: 고정 결제 금액 카드 */}
          <div className="lg:col-span-4">
            <div>
              <h2 className="text-xl font-bold mb-4">최종 결제금액</h2>
            </div>
            <div className="sticky top-12 p-6 border border-gray-200 rounded-sm">
              <div className="flex justify-between items-center mb-2">
                <span>총 상품 금액</span>
                <span className="font-medium text-gray-900">
                  {totalPrice.toLocaleString()}원
                </span>
              </div>
              <div className="space-y-2 pl-4 mb-2">
                {/* 상품 금액 상세 */}

                <div className="flex justify-between items-center text-sm text-gray-600">
                  <span>상품 금액</span>
                  <span>{price.toLocaleString()}원</span>
                </div>

                <div className="flex justify-between items-center text-sm text-gray-600">
                  <span>상품 할인 금액</span>
                  <span>-{discountPrice.toLocaleString()}원</span>
                </div>

                <div className="flex justify-between items-center text-sm text-gray-600">
                  <span>추가 옵션</span>
                  <span>{optionPrice.toLocaleString()}원</span>
                </div>
              </div>

              <div className="flex justify-between items-center mb-4">
                <span>배송비</span>
                <span>
                  {deliveryFee === 0
                    ? '무료'
                    : deliveryFee.toLocaleString() + '원'}
                </span>
              </div>

              {/* 최종 결제 금액 */}
              <div className="flex justify-between items-center ">
                <span className="font-bold">최종 결제 금액</span>
                <span className="text-xl font-bold">
                  {paymentPrice.toLocaleString()}원
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
