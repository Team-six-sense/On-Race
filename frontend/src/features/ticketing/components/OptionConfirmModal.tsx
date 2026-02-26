import { Badge } from '@/components/shadcn/badge';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import React from 'react';

interface EventData {
  course: string;
  pace: string;
}

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  data: EventData;
}

export const OptionConfirmModal = ({
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
          <h3 className="text-lg font-bold text-gray-900">선택 옵션 확인</h3>

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

        <div className="px-6 py-2 font-bold text-lg">
          <p className="text-sm font-medium text-gray-800">
            결제 전 선택하신 옵션을 확인해주세요
          </p>
          <p className="text-sm font-medium text-gray-400">
            이벤트 당일 변경이 불가능합니다.
          </p>
        </div>

        {/* 본문: 사용자 정보 */}
        <div className="px-6 py-2 space-y-4 ">
          <div className="p-3 grid bg-gray-100 rounded-sm">
            <p>선택한 코스</p>
            <p>{data.course}</p>
            <p>한강공원 순환 코스 (평지)</p>
          </div>
        </div>

        <div className="px-6 py-2 space-y-4 ">
          <div className="p-3 grid bg-gray-100 rounded-sm">
            <p>목표 페이스</p>
            <p>{data.pace}</p>
            <p>예상 완주 시간: 1시간 5분</p>
          </div>
        </div>

        <div className="px-6 py-2 space-y-4 ">
          <div className="p-3 grid grid-cols-3">
            <InfoLabel>총 결제금약</InfoLabel>
            <InfoValue>50,000 원</InfoValue>
          </div>
        </div>

        {/* 하단 버튼 */}
        <div className="flex gap-3 px-6 pt-2 pb-6">
          <Button variant="outline" rounded="sm" onClick={onClose}>
            정보 수정
          </Button>
          <Button variant="primary1" rounded="sm" onClick={onConfirm}>
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
  <span className="col-span-2 text-sm text-black">{children}</span>
);
