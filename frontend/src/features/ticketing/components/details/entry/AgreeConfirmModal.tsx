'use client';

import { Label } from '@/components/shadcn/label';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { useEffect, useRef, useState } from 'react';
import { LuInfo, LuX } from 'react-icons/lu';

interface AgreementModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

export const AgreeConfirmModal = ({
  isOpen,
  onClose,
  onConfirm,
}: AgreementModalProps) => {
  const modalRef = useRef<HTMLDivElement>(null);
  const [agreements, setAgreements] = useState({
    terms: false,
    privacy: false,
  });

  // 전체 동의 체크 여부 확인
  const isAllChecked = agreements.terms && agreements.privacy;

  useEffect(() => {
    if (isOpen) {
      setAgreements({
        terms: false,
        privacy: false,
      });
      modalRef.current?.focus();
    }
  }, [isOpen]);

  // 체크박스 핸들러
  const handleCheckboxChange = (id: keyof typeof agreements) => {
    setAgreements((prev) => ({
      ...prev,
      [id]: !prev[id],
    }));
  };
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Tab' || e.key === 'Enter' || e.key === ' ') {
      e.preventDefault(); // 브라우저 기본 동작 방지
      e.stopPropagation(); // 이벤트 전파 방지
    }
  };

  // 모달이 닫혀있으면 아무것도 렌더링하지 않음
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-hidden">
      {/* 배경 레이어: 회색 투명 배경 + 블러 (클릭 이벤트 없음) */}
      <div className="absolute inset-0 bg-black/50 transition-opacity" />

      {/* 모달 본체 */}
      <div
        ref={modalRef}
        onKeyDown={handleKeyDown}
        tabIndex={-1}
        className="relative w-full max-w-lg px-8 py-6 transform overflow-hidden rounded-2xl bg-white shadow-2xl transition-all animate-in fade-in zoom-in duration-300"
      >
        {/* 헤더 부분: 제목과 X 버튼 */}
        <div className="flex w-full items-center justify-between">
          <div className="flex-1 text-2xl font-bold text-gray-900">
            이벤트 참가 약관 동의
          </div>
          <div>
            <Button
              variant="ghost"
              size="iconSm"
              onClick={onClose}
              tabIndex={-1}
            >
              <LuX />
            </Button>
          </div>
        </div>
        <div></div>

        {/* 본문: 사용자 정보 */}
        <div className="px-8 py-6 space-y-4">
          {/* 참가 약관 */}
          <section className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="terms"
                variant="primary"
                checked={agreements.terms}
                onCheckedChange={() => handleCheckboxChange('terms')}
                tabIndex={-1}
              />
              <Label
                htmlFor="terms"
                className="text-sm font-semibold text-gray-700"
              >
                [필수] 참가 약관 동의
              </Label>
            </div>
            <div className="h-32 overflow-y-auto rounded-md border border-gray-200 bg-gray-50 p-3 text-xs text-gray-600 leading-relaxed">
              제1조 (신청 및 참가자 선정) <br />
              1. 본 대회 참가 신청은 다음 각 호의 방식 중 하나로 진행됩니다.
              <br />
              &ensp;①응모 신청 방식 <br />
              &ensp;② 선착 신청 방식 <br />
              2. 응모 신청 방식의 경우 <br />
              &ensp;① 사전 응모 후 무작위 추첨으로 참가자를 선정합니다. <br />
              &ensp;② 응모 완료는 참가 확정을 의미하지 않습니다. <br />
              3. 선착 신청 방식의 경우 ① 결제 완료 순으로 참가가 확정됩니다.
              <br />
              4.참가 확정 여부 및 선정 결과는 마이페이지 및 서비스 내 공지, 알림
              등을 통해 안내될 수 있습니다. <br />
              5. 회사는 신청 및 선정 과정의 공정성 유지를 위하여 필요한
              기술적·관리적 조치를 시행합니다.
            </div>
          </section>

          {/* 개인정보 처리 동의 */}
          <section className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="privacy"
                variant="primary"
                checked={agreements.privacy}
                onCheckedChange={() => handleCheckboxChange('privacy')}
                tabIndex={-1}
              />
              <Label
                htmlFor="privacy"
                className="text-sm font-semibold text-gray-700"
              >
                [필수] 개인정보 처리 동의
              </Label>
            </div>
            <div className="h-26 overflow-y-auto rounded-md border border-gray-200 bg-gray-50 p-3 text-xs text-gray-600 leading-relaxed">
              제1조 (제공받는 제3자) <br />
              회사와 단체상해보험 계약을 체결한 보험사이며, 계약 확정 후 서비스
              제2조 (제공하는 개인정보 항목) <br />
              이름, 생년월일, 성별, 휴대폰번호, 이메일주소 <br />
            </div>
          </section>

          <section className="space-y-2">
            <div className="rounded-md border border-gray-200 bg-gray-50 p-3 text-xs text-gray-600 leading-relaxed">
              <div className="flex items-center gap-3">
                <LuInfo size={25} />
                <span>
                  결제 미완료 시 위 동의 과정에서 수집된 개인정보는 7일 이내에
                  파기됩니다.
                </span>
              </div>
            </div>
          </section>
        </div>

        {/* 푸터 (버튼 영역) */}
        <div className="flex">
          <Button
            variant="primary1"
            rounded="full"
            disabled={!isAllChecked}
            onClick={onConfirm}
            tabIndex={-1}
          >
            확인
          </Button>
        </div>
      </div>
    </div>
  );
};
