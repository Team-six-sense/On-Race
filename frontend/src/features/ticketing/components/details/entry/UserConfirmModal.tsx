import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import React from 'react';
import { LuX } from 'react-icons/lu';

interface UserData {
  name: string;
  birthDate: string;
  gender: string;
  phone: string;
  email: string;
}

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  data: UserData;
  template: number;
}

export const UserConfirmModal = ({
  isOpen,
  onClose,
  onConfirm,
  data,
  template,
}: ModalProps) => {
  if (!isOpen) return null;

  return (
    // 전체 화면 레이어 (z-index 50)
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-hidden">
      {/* 배경 레이어: 회색 투명 배경 + 블러 (클릭 이벤트 없음) */}
      <div className="absolute inset-0 bg-black/50 transition-opacity" />

      {/* 모달 본체 */}
      <div className="relative w-full max-w-lg px-8 py-6 transform overflow-hidden rounded-2xl bg-white shadow-2xl transition-all animate-in fade-in zoom-in duration-300">
        {/* 헤더 부분: 제목과 X 버튼 */}
        <div className="relative flex flex-col items-start justify-between">
          <div className="flex w-full items-center justify-between">
            <div className="flex-1 text-2xl font-bold text-gray-900">
              참가자 정보 확인
            </div>
            <div>
              <Button variant="ghost" size="iconSm" onClick={onClose}>
                <LuX />
              </Button>
            </div>
          </div>
          <div>
            <p className="text-xs text-gray-500">
              신청 전 참가자 정보를 확인해주세요. 하기 내용은 마이페이지에서
              수정이 가능합니다.
            </p>
          </div>
        </div>

        {/* 본문: 사용자 정보 */}
        <div className="px-8 py-6 space-y-4">
          <div className="grid grid-cols-3 gap-2">
            <InfoLabel>이름</InfoLabel>
            <InfoValue>{data.name}</InfoValue>

            <InfoLabel>성별</InfoLabel>
            <InfoValue>{data.gender}</InfoValue>

            <InfoLabel>생년월일</InfoLabel>
            <InfoValue>{data.birthDate}</InfoValue>

            <InfoLabel>휴대폰번호</InfoLabel>
            <InfoValue>{data.phone}</InfoValue>

            <InfoLabel>이메일</InfoLabel>
            <InfoValue>{data.email}</InfoValue>
          </div>
        </div>

        {/* 하단 버튼 */}
        <div className="flex">
          <Button variant="primary1" rounded="full" onClick={onConfirm}>
            확인
          </Button>
        </div>
      </div>
    </div>
  );
};

// 가독성을 위한 내부 컴포넌트들
const InfoLabel = ({ children }: { children: React.ReactNode }) => (
  <span className="text-sm font-medium text-gray-400">{children}</span>
);

const InfoValue = ({ children }: { children: React.ReactNode }) => (
  <span className="col-span-2 text-sm font-semibold text-gray-800">
    {children}
  </span>
);
