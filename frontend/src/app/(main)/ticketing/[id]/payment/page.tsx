'use client';

import React, { useEffect, useState } from 'react';
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
import {
  loadTossPayments,
  TossPaymentsPayment,
} from '@tosspayments/tosspayments-sdk';
import { MdAccessTime, MdImage } from 'react-icons/md';
import { DeliveryFeeTooltip } from '@/features/ticketing/components/DeliveryFeeTooltip';
import { useEventStore } from '@/features/event/store/useEventStore';
import { MultiProductSelect } from '@/features/order/components';
import { cn } from '@/lib/utils';
import { formatKoreanDate } from '@/features/ticketing/utils/date';
import {
  Modal,
  ModalContent,
  ModalDescription,
  ModalFooter,
  ModalHeader,
  ModalTitle,
} from '@/components/ui/modal';

const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY!;

export default function CheckoutPage() {
  const params = useParams();
  const router = useRouter();
  const [isError, setIsError] = useState(false);
  const [openModal, setOpenModal] = useState(false);
  const {
    event,
    eventDetails,
    eventSaleInfo,
    course,
    pace,
    selectedBasicOption,
    setSelectedBasicOption,
    selectedOptions,
    setSelectedOptions,
  } = useEventStore();

  const [payment, setPayment] = useState<TossPaymentsPayment | null>(null);
  const [selectedPayment, setSelectedPayment] = useState('KAKAOPAY');

  const optionItem = [
    { value: 'option1', label: '선택안함', price: 0 },
    { value: 'option2', label: '텀블러', price: 15000 },
    { value: 'option3', label: '스포츠타올', price: 12000 },
    { value: 'option4', label: '러닝화', price: 89000 },
  ];

  const price = eventDetails?.courses?.[0]?.price ?? 0;
  const title = event?.title ?? '결제 상품';
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

  // const [timeLeft, setTimeLeft] = useState(600);

  const formatTime = (seconds: number) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  };

  const discountParticipationFee = () => {
    const ratio = 1 - discountRate / 100;
    return (price * ratio).toLocaleString();
  };

  const handlePayment = () => {
    requestPayment(selectedPayment);
  };

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

  async function requestPayment(selectedMethod: string) {
    if (!payment) return;

    // 공통 파라미터
    const baseOptions = {
      amount: {
        currency: 'KRW',
        value: paymentPrice,
      },
      orderId: `order-${Math.random().toString(36).slice(2, 11)}`,
      orderName: title,
      successUrl: `${window.location.origin}/ticketing/${params.id}/payment/completed`,
      failUrl: `${window.location.origin}/payment/fail`,
    };

    try {
      switch (selectedMethod) {
        case 'CARD':
          await payment.requestPayment({
            method: 'CARD',
            ...baseOptions,
            card: {
              flowMode: 'DEFAULT',
              useEscrow: false,
            },
          });
          break;

        case 'VIRTUAL_ACCOUNT':
          await payment.requestPayment({
            method: 'VIRTUAL_ACCOUNT',
            ...baseOptions,
            virtualAccount: {
              customerName: '김토스',
              validHours: 24,
              cashReceipt: { type: '소득공제' },
            },
          } as any);
          break;

        case 'TRANSFER':
          await payment.requestPayment({
            method: 'TRANSFER',
            ...baseOptions,
            transfer: {
              cashReceipt: { type: '소득공제' },
            },
          });
          break;

        case 'MOBILE_PHONE':
          await payment.requestPayment({
            method: 'MOBILE_PHONE',
            ...baseOptions,
          });
          break;

        case 'KAKAOPAY':
          await payment.requestPayment({
            method: 'CARD',
            ...baseOptions,
            card: {
              flowMode: 'DIRECT',
              easyPay: 'KAKAOPAY',
            },
          });
          break;
      }
    } catch (error) {
      console.error('결제 요청 실패:', error);
    }
  }

  useEffect(() => {
    async function fetchPayment() {
      try {
        // SDK 초기화
        const tossPayments = await loadTossPayments(clientKey);

        // 결제 객체 설정 (고객 ID는 실제 서비스의 유저 ID 혹은 고유값 사용)
        const paymentInstance = tossPayments.payment({
          customerKey: 'ANONYMOUS', // 비회원일 경우 ANONYMOUS
        });

        setPayment(paymentInstance);
      } catch (error) {
        console.error('Error fetching payment instance:', error);
      }
    }

    fetchPayment();
  }, []);

  useEffect(() => {
    setIsError(false);
  }, [event?.thumbnailImg]);

  // useEffect(() => {
  //   if (timeLeft <= 0) {
  //     setOpenModal(true);
  //     // router.push('/'); // 시간 만료 시 튕겨나갈 주소
  //   }

  //   const timer = setInterval(() => {
  //     setTimeLeft((prev) => prev - 1);
  //   }, 1000);

  //   return () => clearInterval(timer);
  // }, [timeLeft, router]);

  return (
    <div className="bg-white max-w-7xl mx-auto px-30">
      <div className="w-full bg-black text-white p-3 mb-2">
        <div className="flex items-center text-sm text-font-accent">
          <MdAccessTime className="mr-1" />

          <span>남은 시간 09:59</span>
          <span className="px-2 text-base text-white ">
            시간 내 결제를 완료하지 않으면 자동 취소됩니다
          </span>
        </div>
      </div>
      <div className="max-w-7xl mx-auto py-4 ">
        <header className="text-black">
          <h2 className="text-3xl font-bold">주문/결제</h2>
          <div className="my-2 h-[2px] bg-black"></div>
        </header>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-16">
          {/* 왼쪽 영역: 입력 폼 */}
          <div className="lg:col-span-8">
            {/* 주문 상품 정보 */}
            <FormSection title="주문상품">
              <div className="flex flex-col gap-4">
                <div className="flex gap-6">
                  <div className="w-30 h-30 flex-shrink-0">
                    {!event?.thumbnailImg || isError ? (
                      <div className="w-full h-full flex flex-col items-center justify-center text-gray-400 bg-gray-50">
                        <MdImage size={100} />
                        <p className="mt-2 text-sm">
                          이미지를 불러올 수 없습니다.
                        </p>
                      </div>
                    ) : (
                      <img
                        src={event?.thumbnailImg.url}
                        alt={event?.title}
                        className="w-full h-full object-cover rounded-sm"
                        onError={() => setIsError(true)} // 에러 발생 시 상태 변경
                      />
                    )}
                  </div>
                  <div className="flex-1">
                    <h3 className="text-lg font-bold text-gray-900">
                      {event?.title}
                    </h3>
                    <div className="flex flex-col text-sm ">
                      <span className="flex items-center py-1 rounded text-sm text-gray-500 font-semibold">
                        <span>
                          {formatKoreanDate(event?.eventAt ?? '', true)}
                        </span>
                        <span className="px-1">•</span>
                        <span>{event?.venue}</span>
                      </span>
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
                </div>

                <div className="flex-1 space-y-4">
                  <div className="flex">
                    <span className="w-28 text-base text-font-medium font-medium">
                      코스
                    </span>
                    <span className="flex-1">{course}</span>
                  </div>
                  <div className="flex">
                    <span className="w-28 text-base text-font-medium font-medium">
                      페이스
                    </span>
                    <span className="flex-1">{pace}</span>
                  </div>
                  <div className="flex items-start">
                    <span className="w-28 flex items-center text-base text-font-medium font-medium">
                      기본 옵션1
                    </span>
                    <div className="w-full rounded-sm px-4 pb-0">
                      <span className="text-medium">기념 티셔츠</span>
                      <Select
                        value={selectedBasicOption}
                        onValueChange={setSelectedBasicOption}
                      >
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
                    <span className="w-28 flex items-center text-base text-font-medium font-medium">
                      기본 옵션2
                    </span>
                    <div className="w-full rounded-sm px-4 pb-0 text-medium">
                      완주 메달 (완주자에 한해 현장 증정됩니다.)
                    </div>
                  </div>

                  <div className="flex items-start ">
                    <span className="w-28 flex items-center text-base text-font-medium font-medium">
                      선택 옵션
                    </span>
                    <div className="w-full rounded-sm px-4 pb-0">
                      <MultiProductSelect
                        options={optionItem}
                        selectSize="sm"
                        variant="default"
                        selectedIds={selectedOptions}
                        onSelectionChange={setSelectedOptions}
                      />
                    </div>
                  </div>
                </div>
              </div>
            </FormSection>

            {/* 배송지 정보 */}
            <FormSection title="배송지 정보">
              <div className="flex justify-between items-center">
                <div className="flex flex-wrap gap-2 items-center">
                  <span className="text-lg font-semibold text-black">
                    우리 집
                  </span>
                  <div className="flex gap-1.5">
                    <span className="text-[10px] px-2 py-1 bg-gray-100 text-gray-600 font-bold rounded-sm">
                      기본배송지
                    </span>
                  </div>
                </div>
                {/* <div className="text-base text-font-medium">
                  등록된 배송지가 없습니다
                </div> */}
                <div className="flex">
                  <Button variant="outline" size="sm" rounded="sm">
                    변경하기
                  </Button>
                </div>
              </div>

              {/* 정보 섹션 */}
              <div className="space-y-1">
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
                          <SelectItem value="option1">
                            문 앞에 놓아주세요
                          </SelectItem>
                          <SelectItem value="option2">
                            경비실에 맡겨주세요
                          </SelectItem>
                          <SelectItem value="option3">
                            배송 전 연락주세요
                          </SelectItem>
                          <SelectItem value="option4">직접 수령</SelectItem>
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
                  value={selectedPayment}
                  onValueChange={setSelectedPayment}
                  className="gap-2"
                >
                  {[
                    { id: 'KAKAOPAY', label: '카카오페이' },
                    { id: 'CARD', label: '신용/체크카드' },
                    // { id: 'VIRTUAL_ACCOUNT', label: '무통장입금' },
                    { id: 'MOBILE_PHONE', label: '휴대폰결제' },
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
                <span>{totalPrice.toLocaleString()}원</span>
              </div>
              <div className="space-y-2 pl-4 mb-2">
                {/* 상품 금액 상세 */}

                <div className="flex justify-between items-center text-base text-font-medium">
                  <span>상품 금액</span>
                  <span>{price.toLocaleString()}원</span>
                </div>

                <div className="flex justify-between items-center text-base text-font-medium">
                  <span>상품 할인 금액</span>
                  <span>-{discountPrice.toLocaleString()}원</span>
                </div>

                <div className="flex justify-between items-center text-base text-font-medium">
                  <span>추가 옵션</span>
                  <span>{Number(optionPrice).toLocaleString()}원</span>
                </div>
              </div>

              <div className="flex justify-between items-center mb-4 text-lg">
                <div className="flex items-center text-lg font-medium">
                  <span className="mr-1">배송비</span>
                  <DeliveryFeeTooltip />
                </div>
                <span>
                  {deliveryFee === 0
                    ? '무료'
                    : deliveryFee.toLocaleString() + '원'}
                </span>
              </div>

              {/* 최종 결제 금액 */}
              <div className="flex justify-between items-center mb-4">
                <span className="text-lg font-semibold ">최종 결제 금액</span>
                <span className="text-xl font-bold">
                  {Number(paymentPrice).toLocaleString()}원
                </span>
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
                <Button rounded="full" onClick={handlePayment}>
                  {Number(paymentPrice).toLocaleString()}원 결제하기
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <Modal open={openModal} onOpenChange={setOpenModal}>
        {/* cva를 통해 정의한 size="lg" 적용 */}
        <ModalContent size="md" className="">
          <ModalHeader className="py-2">
            <ModalTitle className="text-3xl font-bold">
              결제시간이 만료되었습니다.
            </ModalTitle>
            <ModalDescription className="text-font-medium">
              페이지를 새로고침하여 다시 시도해주세요ㅕ
            </ModalDescription>
          </ModalHeader>

          <ModalFooter>
            <Button
              variant="primary1"
              rounded="full"
              onClick={() => router.push(`/ticketing/${event?.id}`)}
            >
              확인
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>
    </div>
  );
}
