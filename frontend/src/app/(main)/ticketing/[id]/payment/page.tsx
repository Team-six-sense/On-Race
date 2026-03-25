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
import { useParams, useRouter } from 'next/navigation';

export default function CheckoutPage() {
  const params = useParams();
  const router = useRouter();
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
            주문/결제
          </h1>
        </header>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-16">
          {/* 왼쪽 영역: 입력 폼 */}
          <div className="lg:col-span-8">
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
                      <RadioGroup
                        value={selectedPayment}
                        onValueChange={setSelectedPayment}
                        className="gap-2" // 항목 간 간격 조절
                      >
                        {marathonItem.options.map((item) => (
                          <label
                            key={item.id}
                            htmlFor={item.id}
                            className="flex items-center justify-between rounded-lg cursor-pointer"
                          >
                            <div className="flex items-center gap-3">
                              <RadioGroupItem value={item.id} id={item.id} />
                              <span className="text-sm font-medium text-gray-900">
                                {item.label}
                              </span>
                            </div>

                            <div className="text-right">
                              <span className={`text-sm font-semibold`}>
                                +{item.price.toLocaleString()}원
                              </span>
                            </div>
                          </label>
                        ))}
                      </RadioGroup>
                    </div>
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
                    변경하기
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
                      <Select>
                        <SelectTrigger variant="default" selectSize="sm">
                          <SelectValue placeholder="배송 요청사항을 선택해주세요" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="option1">요청사항1</SelectItem>
                          <SelectItem value="option2">요청사항2</SelectItem>
                          <SelectItem value="option3">요청사항3</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>
                </div>
              </div>
            </FormSection>

            {/* 결제 수단 */}
            <FormSection title="결제 수단">
              <div className="flex items-center">
                <RadioGroup
                  // value={selectedValue}
                  // onValueChange={setSelectedValue}
                  className="gap-3"
                >
                  {[
                    { id: '1', label: '카카오페이' },
                    { id: '2', label: '신용/체크카드' },
                    { id: '3', label: '무통장입금' },
                    { id: '4', label: '휴대폰결제' },
                  ].map((item) => (
                    <label
                      key={item.id}
                      htmlFor={item.id}
                      className="flex items-center "
                    >
                      <RadioGroupItem
                        className="mx-2"
                        value={item.id}
                        id={item.id}
                      />
                      <span className="">{item.label}</span>
                    </label>
                  ))}
                </RadioGroup>
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
              <div className="flex justify-between items-center mb-4">
                <span className="font-bold">최종 결제 금액</span>
                <span className="text-xl font-bold">51,000원</span>
              </div>

              <div className="h-[1px] bg-gray-200 my-6 w-full" />

              {/* 결제 확인 및 버튼 섹션 */}
              <div className="space-y-2 mb-4">
                <div className="space-y-3">
                  <div className="flex items-center space-x-1">
                    <Checkbox id="all" />
                    <label htmlFor="all" className="text-sm font-bold">
                      결제 내용을 모두 확인했으며, 아래 사항에 모두 동의합니다.
                    </label>
                  </div>

                  <div className="flex items-center space-x-1">
                    <Checkbox id="option1" />
                    <label htmlFor="option1" className="text-sm">
                      (필수) 결제 대행 서비스 이용약관
                    </label>
                  </div>
                  <div className="flex items-center space-x-1">
                    <Checkbox id="option2" />
                    <label htmlFor="option2" className="text-sm">
                      (필수) 개인정보 처리 및 수집
                    </label>
                  </div>
                  <div className="flex items-center space-x-1">
                    <Checkbox id="option3" />
                    <label htmlFor="option3" className="text-sm ">
                      (필수) 예매 취소 및 환불 정책 동의
                    </label>
                  </div>
                </div>
              </div>

              <div>
                <Button
                  rounded="full"
                  onClick={() =>
                    router.push(`/ticketing/${params.id}/completed`)
                  }
                >
                  45,000원 결제하기
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
