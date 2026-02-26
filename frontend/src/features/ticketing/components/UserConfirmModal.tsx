import { Button } from '@/components/ui/button';
import React from 'react';

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
}

export const UserConfirmModal = ({
  isOpen,
  onClose,
  onConfirm,
  data,
}: ModalProps) => {
  if (!isOpen) return null;

  return (
    // 전체 화면 레이어 (z-index 50)
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-hidden">
      {/* 1. 배경 레이어: 회색 투명 배경 + 블러 (클릭 이벤트 없음) */}
      <div className="absolute inset-0 bg-black/50 transition-opacity" />

      {/* 2. 모달 본체 */}
      <div className="relative w-full max-w-md transform overflow-hidden rounded-2xl bg-white shadow-2xl transition-all animate-in fade-in zoom-in duration-300">
        {/* 헤더 부분: 제목과 X 버튼 */}
        <div className="relative flex items-center justify-between border-b border-gray-100 px-6 py-4">
          <h3 className="text-lg font-bold text-gray-900">사용자 정보 확인</h3>

          {/* X 닫기 버튼 */}
          <button
            onClick={onClose}
            className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
            aria-label="닫기"
          >
            <svg
              className="h-6 w-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        {/* 본문: 사용자 정보 */}
        <div className="px-6 py-6 space-y-4">
          <div className="grid grid-cols-3 gap-2">
            <InfoLabel>이름</InfoLabel>
            <InfoValue>{data.name}</InfoValue>

            <InfoLabel>생년월일</InfoLabel>
            <InfoValue>{data.birthDate}</InfoValue>

            <InfoLabel>성별</InfoLabel>
            <InfoValue>{data.gender}</InfoValue>

            <InfoLabel>연락처</InfoLabel>
            <InfoValue>{data.phone}</InfoValue>

            <InfoLabel>이메일</InfoLabel>
            <InfoValue>{data.email}</InfoValue>
          </div>
        </div>

        {/* 하단 버튼 */}
        <div className="flex gap-3 px-6 py-4">
          <Button variant="outline" onClick={onClose}>
            정보 수정
          </Button>
          <Button variant="primary1" onClick={onConfirm}>
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
