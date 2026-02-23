'use client';

import { Button } from '@/components/ui/button';
import { LuArrowLeft } from 'react-icons/lu';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radioGroup';
import { Checkbox } from '@/components/ui/checkbox';
import { useRouter } from 'next/navigation';

export default function OrderDetailPage() {
  const router = useRouter();
  return (
    <div className="max-w-6xl mx-auto px-4 pt-8 pb-0">
      {/* 1. 상단 네비게이션: 목록으로 가기 */}
      <div className="flex justify-self-start">
        <Button variant="text">
          <LuArrowLeft size={20} />
          뒤로가기
        </Button>
      </div>

      {/* 주문 정보 카드 */}
      <div className="bg-primary p-6 rounded-none border-3 border-gray-300 mb-6">
        <div className="flex justify-between items-center mb-4">
          <span className="text-lg text-black">주문 정보</span>
        </div>

        <div className="flex justify-between items-center mb-4">
          <span className="text-sm text-gray-500">이벤트 명</span>
          <span className="text-sm text-black">서울 마라톤 대회 2026</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-sm text-gray-500">코스</span>
          <span className="text-sm text-black">풀코스 (42.195KM)</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-sm text-gray-500">페이스</span>
          <span className="text-sm text-black">중급(6:00~7:00/km)</span>
        </div>
        <div className="flex justify-between items-center mb-4">
          <span className="text-sm text-gray-500">참가비</span>
          <span className="text-sm text-black">50,000원</span>
        </div>
      </div>

      {/* 추가 옵션 섹션 */}
      <section className="bg-primary rounded-none border-3 border-gray-300 mb-6">
        <div className="p-6 ">
          <div className="flex items-center mb-2">
            <span className="text-lg text-black">추가 옵션</span>
          </div>
          <div className="flex justify-between items-center p-2 mb-2 bg-gray-100 border-3 border-gray-200 rounded-none">
            <span className="text-sm text-gray-500">기념 티셔츠 (XL) </span>
            <span className="text-sm text-black">15,000원</span>
          </div>
          <div className="flex justify-between items-center p-2 mb-2 bg-gray-100 border-3 border-gray-200 rounded-none">
            <span className="text-sm text-gray-500">스포츠 타월</span>
            <span className="text-sm text-black">8,000원</span>
          </div>
        </div>
      </section>

      {/* 결제 수단 섹션 */}
      <section className="bg-primary rounded-none border-3 border-gray-300 mb-6">
        <div className="p-6">
          <h3 className="text-lg text-black mb-2">결제 수단</h3>
          <RadioGroup
            // value={selectedValue}
            // onValueChange={setSelectedValue}
            className="gap-3"
          >
            {[
              { id: '1', label: '신용카드' },
              { id: '2', label: '계좌이체' },
              { id: '3', label: '휴대폰결제' },
              { id: '4', label: '카카오페이' },
            ].map((item) => (
              <label
                key={item.id}
                htmlFor={item.id}
                className="flex items-center space-x-3 p-3 border border-gray-300 rounded-none"
              >
                <RadioGroupItem value={item.id} id={item.id} />
                <span className="text-sm font-medium">{item.label}</span>
              </label>
            ))}
          </RadioGroup>
        </div>
      </section>

      {/* 결제 정보 섹션 */}
      <section className="bg-gray-100 rounded-none border-2 border-gray-300 mb-6">
        <div className="p-6 ">
          <div className="flex items-center mb-2">
            <span className="text-lg text-black">결제 정보</span>
          </div>
          <div className="flex justify-between items-center p-2 mb-2">
            <span className="text-sm text-gray-500">상품 구매액</span>
            <span className="text-sm text-black">73,000원</span>
          </div>
          <div className="flex justify-between items-center p-2 mb-2">
            <span className="text-sm text-gray-500">배송비</span>
            <span className="text-sm text-black">3,000원</span>
          </div>
          <div className="flex justify-between items-center p-2 mb-2">
            <span className="text-sm text-gray-500">할인 혜택 금액</span>
            <span className="text-sm text-red-500">-5,000원</span>
          </div>
          <div className="flex justify-between items-center p-2 mb-2 border-t">
            <span className="text-lg text-gray-500">총 결제 금액</span>
            <span className="text-lg text-black">71,000원</span>
          </div>
        </div>
      </section>

      {/* 결제 동의 섹션 */}
      <section className="bg-primary rounded-none border-2 border-gray-300 mb-6">
        <div className="px-6 py-4">
          <div className="flex items-center mb-2">
            <Checkbox className="mr-2" />
            <span className="text-lg text-black">결제 진행 필수 동의</span>
          </div>
          <div className="flex justify-between items-center p-2">
            <span className="text-sm text-gray-500">
              개인정보 수집 및 동의, 결제대행 서비스 이용약관에 동의합니다.
            </span>
          </div>
        </div>
      </section>

      {/* 하단 버튼 영역 */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        <Button
          variant="outline"
          rounded="sm"
          onClick={() => router.push('/schedule')}
        >
          취소
        </Button>
        <Button
          variant="primary1"
          rounded="sm"
          onClick={() => router.push('/ticketing/eventId/completed')}
        >
          결제하기
        </Button>
      </div>

      {/* 결제 동의 섹션 */}
      <section className="bg-blue-50 rounded-none border-2 border-blue-200 mb-6">
        <div className="px-6 py-4">
          <div className="flex items-center mb-2">
            <span className="text-lg text-black">응모 이벤트 안내</span>
          </div>
          <div>
            <ul className="list-disc pl-5 ">
              <li>응모 이벤트의 경우 당첨 시 결제 안내가 전송됩니다.</li>
              <li>당첨 안내: 휴대폰 문자, 이메일, 웹 팝업</li>
              <li>당첨 후 3일 이내 미결제 시 자동 취소됩니다.</li>
            </ul>
          </div>
        </div>
      </section>
    </div>
  );
}
