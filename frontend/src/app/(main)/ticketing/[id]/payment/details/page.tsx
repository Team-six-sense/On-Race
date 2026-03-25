'use client';

import React, { useState } from 'react';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radioGroup';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';

export default function CheckoutPage() {
  const [selectedOption, setSelectedOption] = useState('none');
  const [selectedPayment, setSelectedPayment] = useState('card');

  // 데이터 설정
  const marathonItem = {
    id: 1,
    title: '2024 제10회 서울 릴레이 마라톤',
    course: 'Half Course (21.0975km)',
    pace: '05:00 (Sub-5 페이스러너)',
    price: 55000,
    image: '/image/default.png',
    options: [
      { id: 'option1', label: '기념 티셔츠 (기본 구성)', price: 0 },
      { id: 'option2', label: '기념 티셔츠 + 텀블러', price: 14000 },
      {
        id: 'option3',
        label: '기념 티셔츠 + 텀블러 + 스포츠 타올',
        price: 32000,
      },
    ],
  };

  const optionPrice =
    marathonItem.options.find((o) => o.id === selectedOption)?.price || 0;
  const total = marathonItem.price + optionPrice;

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

  return (
    <div className="bg-white py-4 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto">
        <header className="mb-4">
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
            결제 상세내역
          </h1>
        </header>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-16">
          {/* 왼쪽 영역: 입력 폼 */}
          <div className="lg:col-span-8">
            <FormSection title="주문현황">
              <div className="flex flex-col gap-4">
                <div className="flex-1 space-y-2">
                  <div className="flex">
                    <span className="w-28 text-gray-600">주문번호</span>
                    <span className="flex-1">ORD20260215001</span>
                  </div>
                  <div className="flex">
                    <span className="w-28 text-gray-600">주문일시</span>
                    <span className="flex-1">2026-02-15 14:30:25</span>
                  </div>
                  <div className="flex items-center">
                    <span className="w-28 text-gray-600">주문상태</span>
                    <span className="flex-1 font-semibold">결제 완료</span>
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
                      src={marathonItem.image}
                      alt={marathonItem.title}
                      className="w-full h-full object-cover rounded-sm"
                    />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-xl font-bold text-gray-900 mb-2">
                      {marathonItem.title}
                    </h3>
                    <div className="flex flex-col text-sm ">
                      <span className="flex items-center py-1 rounded text-xs text-gray-600 font-semibold">
                        2026.02.28 (토) 오전 9시 서울 여의도 공원
                      </span>
                      <span className="flex items-center py-1 rounded text-md font-bold">
                        48,000원
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex-1 space-y-2">
                  <div className="flex">
                    <span className="w-28 text-gray-600">코스</span>
                    <span className="flex-1">{marathonItem.course}</span>
                  </div>
                  <div className="flex">
                    <span className="w-28 text-gray-600">페이스</span>
                    <span className="flex-1">{marathonItem.pace}</span>
                  </div>
                  <div className="flex">
                    <span className="w-28 text-gray-600 flex  items-center">
                      추가 옵션
                    </span>
                    <div className="w-full bg-gray-100 rounded-sm p-3">
                      <label className="flex items-center justify-between rounded-lg cursor-pointer">
                        <div className="flex items-center gap-3">
                          <span className="text-sm font-medium text-gray-900">
                            기념 티셔츠 (기본구성)
                          </span>
                        </div>

                        <div className="text-right">
                          <span className={`text-sm font-semibold`}>+0원</span>
                        </div>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            </FormSection>

            {/* 참가자 정보 */}
            <FormSection title="참가자 정보">
              <div className="flex flex-col gap-4">
                <div className="flex-1 space-y-2">
                  <div className="flex">
                    <span className="w-28 text-gray-600">이름</span>
                    <span className="flex-1">김유저</span>
                  </div>
                  <div className="flex">
                    <span className="w-28 text-gray-600">성별</span>
                    <span className="flex-1">여</span>
                  </div>
                  <div className="flex items-center">
                    <span className="w-28 text-gray-600">생년월일</span>
                    <span className="flex-1 font-semibold">1999년 1월 1일</span>
                  </div>
                  <div className="flex items-center">
                    <span className="w-28 text-gray-600">휴대폰번호</span>
                    <span className="flex-1 font-semibold">010-1234-5678</span>
                  </div>
                  <div className="flex items-center">
                    <span className="w-28 text-gray-600">이메일</span>
                    <span className="flex-1">user@test.com</span>
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
                    <span className="px-2 py-0.5 bg-blue-50 text-blue-600 text-xs font-semibold rounded-full">
                      기본배송지
                    </span>
                  </div>
                </div>
                <div className="flex">
                  <Button variant="outline" size="sm" rounded="sm">
                    변경
                  </Button>
                </div>
              </div>

              {/* 정보 섹션 */}
              <div className="space-y-1">
                {/* 주소 */}
                <div className="flex gap-1">
                  <div className="text-sm font-semibold">
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
                      <p className="text-sm text-gray-500">배송요청사항</p>
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
              <div className="flex-1 space-y-3">
                <div className="flex items-center">
                  <span className="text-lg   font-bold">신용/체크카드</span>
                </div>
                <div className="flex">
                  <span className="w-28 text-gray-600">카드사</span>
                  <span className="flex-1 font-bold">신한</span>
                </div>
                <div className="flex">
                  <span className="w-28 text-gray-600">할부여부</span>
                  <span className="flex-1 font-bold">일시불</span>
                </div>
              </div>
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
                <span className="font-medium text-gray-900">48,000원</span>
              </div>
              <div className="space-y-2 pl-4 mb-2">
                {/* 상품 금액 상세 */}

                <div className="flex justify-between items-center text-sm text-gray-600">
                  <span>상품 금액</span>
                  <span>50,000원</span>
                </div>

                <div className="flex justify-between items-center text-sm text-gray-600">
                  <span>상품 할인 금액</span>
                  <span>-2,000원</span>
                </div>

                <div className="flex justify-between items-center text-sm text-gray-600">
                  <span>추가 옵션</span>
                  <span>0원</span>
                </div>
              </div>

              <div className="flex justify-between items-center mb-4">
                <span>배송비</span>
                <span>3,000원</span>
              </div>

              {/* 최종 결제 금액 */}
              <div className="flex justify-between items-center ">
                <span className="font-bold">최종 결제 금액</span>
                <span className="text-xl font-bold">51,000원</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
