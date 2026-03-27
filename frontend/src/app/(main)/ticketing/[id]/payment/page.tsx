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
import { useParams, useRouter } from 'next/navigation';
import { Label } from '@/components/shadcn/label';
import { MdAccessTime } from 'react-icons/md';
import { LuCircleAlert } from 'react-icons/lu';
import { DeliveryFeeTooltip } from '@/features/ticketing/components/DeliveryFeeTooltip';

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
    <div className="flex flex-col md:flex-row border-b border-gray-100 py-8 last:border-0">
      <div className="w-full md:w-1/5 mb-4 md:mb-0 flex items-center gap-2 self-start">
        <h2 className="text-lg font-bold text-gray-800">{title}</h2>
      </div>
      <div className="w-full md:w-4/5">{children}</div>
    </div>
  );

  return (
    <div className="bg-white ">
      <div className="w-full bg-black text-white py-3 px-20 mb-2">
        <div className="flex items-center text-sm text-font-accent">
          <MdAccessTime className="mr-1" />

          <span>남은 시간 00:09:59</span>
          <span className="px-2 text-base text-white ">
            시간 내 결제를 완료하지 않으면 자동 취소됩니다
          </span>
        </div>
      </div>
      <div className="max-w-6xl mx-auto py-4 px-2">
        <header className="text-black px-4">
          <h2 className="text-3xl font-bold">주문/결제</h2>
          <div className="my-2 h-[2px] bg-black"></div>
        </header>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-16 px-6">
          {/* 왼쪽 영역: 입력 폼 */}
          <div className="lg:col-span-8">
            {/* 주문 상품 정보 */}
            <FormSection title="주문상품">
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
                    <h3 className="text-lg font-bold text-gray-900">
                      {marathonItem.title}
                    </h3>
                    <div className="flex flex-col text-sm ">
                      <span className="flex items-center py-1 rounded text-sm text-gray-500 font-semibold">
                        2026.02.28 (토) 오전 9시 서울 여의도 공원
                      </span>
                      <span className="flex items-center text-base font-semibold">
                        48,000원
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex-1 space-y-2">
                  <div className="flex">
                    <span className="w-28 text-base text-font-medium font-medium">
                      코스
                    </span>
                    <span className="flex-1">{marathonItem.course}</span>
                  </div>
                  <div className="flex">
                    <span className="w-28 text-base text-font-medium font-medium">
                      페이스
                    </span>
                    <span className="flex-1">{marathonItem.pace}</span>
                  </div>
                  <div className="flex items-start">
                    <span className="w-28 pt-3 flex items-center text-base text-font-medium font-medium">
                      기본 옵션1
                    </span>
                    <div className="w-full rounded-sm p-3 pb-0">
                      <span className="text-medium">기본 티셔츠</span>
                      <Select>
                        <SelectTrigger variant="default" selectSize="sm">
                          <SelectValue placeholder="사이즈를 선택해주세요" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="option1">S(90~95)</SelectItem>
                          <SelectItem value="option2">M(95~100)</SelectItem>
                          <SelectItem value="option3">L(100~105)</SelectItem>
                          <SelectItem value="option4">XL(105~120)</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <div className="flex items-start">
                    <span className="w-28 pt-3 flex items-center text-base text-font-medium font-medium">
                      기본 옵션2
                    </span>
                    <div className="w-full rounded-sm p-3 pb-0 text-medium">
                      완주 메달 (완주자에 한해 현장 증정됩니다.)
                    </div>
                  </div>

                  <div className="flex">
                    <span className="w-28 pt-3 flex items-center text-base text-font-medium font-medium">
                      선택 옵션
                    </span>
                    <div className="w-full rounded-sm p-3 pb-0">
                      <Select>
                        <SelectTrigger variant="default" selectSize="sm">
                          <SelectValue placeholder="상품을 선택해주세요" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="option1">선택안함</SelectItem>
                          <SelectItem value="option2">텀블러</SelectItem>
                          <SelectItem value="option3">스포츠타올</SelectItem>
                          <SelectItem value="option3">러닝화</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>
                </div>
              </div>
            </FormSection>

            {/* 배송지 정보 */}
            <FormSection title="배송지 정보">
              <div className="flex justify-between items-center">
                {/* <div className="flex flex-wrap gap-2 items-center">
                  <span className="text-lg font-semibold text-black">
                    우리 집
                  </span>
                  <div className="flex gap-1.5">
                    <span className="px-2 py-0.5 bg-blue-50 text-blue-600 text-xs font-semibold rounded-full">
                      기본배송지
                    </span>
                  </div>
                </div> */}
                <div className="text-base text-font-medium">
                  등록된 배송지가 없습니다
                </div>
                <div className="flex">
                  <Button variant="outline" size="sm" rounded="sm">
                    변경하기
                  </Button>
                </div>
              </div>

              {/* 정보 섹션 */}
              {/* <div className="space-y-1">
                
                <div className="flex gap-1">
                  <div className="text-base font-medium">
                    서울특별시 강남구 테헤란로 123, 좋은아파트 102동 304호
                  </div>
                </div>

                
                <div className="flex items-center gap-3">
                  <span className="text-sm text-gray-500 font-medium">
                    김유저 - 010-1234-5678
                  </span>
                </div>

                <div className="pt-3">
                  <div className="flex items-center gap-3">
                    <div>
                      <p className="text-base text-font-medium font-medium">
                        배송요청사항
                      </p>
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
              </div> */}
            </FormSection>

            {/* 결제 수단 */}
            <FormSection title="결제 수단">
              <div className="flex items-center">
                <RadioGroup
                  // value={selectedValue}
                  // onValueChange={setSelectedValue}
                  className="gap-2"
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
                      className="flex items-center text-base font-medium"
                    >
                      <RadioGroupItem
                        className="mr-2"
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
              <h2 className="text-xl font-bold pt-6 pb-4">최종 결제금액</h2>
            </div>
            <div className="sticky top-12 p-6 border border-gray-200 rounded-sm">
              <div className="flex justify-between items-center mb-2 text-lg font-medium">
                <span>총 상품 금액</span>
                <span>48,000원</span>
              </div>
              <div className="space-y-2 pl-4 mb-2">
                {/* 상품 금액 상세 */}

                <div className="flex justify-between items-center text-base text-font-medium">
                  <span>상품 금액</span>
                  <span>50,000원</span>
                </div>

                <div className="flex justify-between items-center text-base text-font-medium">
                  <span>상품 할인 금액</span>
                  <span>-2,000원</span>
                </div>

                <div className="flex justify-between items-center text-base text-font-medium">
                  <span>추가 옵션</span>
                  <span>0원</span>
                </div>
              </div>

              <div className="flex justify-between items-center mb-4 text-lg">
                <div className="flex items-center text-lg font-medium">
                  <span className="mr-1">배송비</span>
                  {/* <LuCircleAlert className="text-gray-400" /> */}
                  <DeliveryFeeTooltip />
                </div>
                <span>3,000원</span>
              </div>

              {/* 최종 결제 금액 */}
              <div className="flex justify-between items-center mb-4">
                <span className="text-lg font-semibold ">최종 결제 금액</span>
                <span className="text-xl font-bold">51,000원</span>
              </div>

              <div className="h-[1px] bg-gray-200 my-6 w-full" />

              {/* 결제 확인 및 버튼 섹션 */}
              <div className="px-1 pb-2">
                {/* py-0.5 또는 py-1로 간격 조절 */}
                <div className="flex items-center justify-between group py-0.5">
                  <div className="flex items-center space-x-3">
                    <Label htmlFor="t1" className="text-sm text-gray-500">
                      (필수) 결제 대행 서비스 이용약관
                    </Label>
                  </div>
                  <Button
                    variant="link"
                    size="fit"
                    className="text-sm text-gray-500 h-auto p-0"
                  >
                    보기
                  </Button>
                </div>

                <div className="flex items-center justify-between group py-0.5">
                  <div className="flex items-center space-x-3">
                    <Label htmlFor="t2" className="text-sm text-gray-500">
                      (필수) 개인정보 처리 및 수집
                    </Label>
                  </div>
                  <Button
                    variant="link"
                    size="fit"
                    className="text-sm text-gray-500 h-auto p-0"
                  >
                    보기
                  </Button>
                </div>

                <div className="flex items-center justify-between group py-0.5">
                  <div className="flex items-center space-x-3">
                    <Label htmlFor="t3" className="text-sm text-gray-500">
                      (필수) 예매 취소 및 환불 정책 동의
                    </Label>
                  </div>
                  <Button
                    variant="link"
                    size="fit"
                    className="text-sm text-gray-500 h-auto p-0"
                  >
                    보기
                  </Button>
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
