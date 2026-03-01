import { Badge } from '@/components/shadcn/badge';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import React from 'react';

interface EventData {
  name: string;
  eventDate: string;
  location: string;
}

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  data: EventData;
  template: number;
}

export const EventConfirmModal = ({
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
      {/* 1. 배경 레이어: 회색 투명 배경 + 블러 (클릭 이벤트 없음) */}
      <div className="absolute inset-0 bg-black/50 transition-opacity" />

      {/* 2. 모달 본체 */}
      <div className="relative w-full max-w-md transform overflow-hidden rounded-2xl bg-white shadow-2xl transition-all animate-in fade-in zoom-in duration-300">
        {/* 헤더 부분: 제목과 X 버튼 */}
        <div className="relative flex items-center justify-between border-b border-gray-100 px-6 py-4">
          <h3 className="text-lg font-bold text-gray-900">
            러닝 이벤트 정보 확인
          </h3>

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

        <div className="px-6 py-2 font-bold text-lg">{data.name}</div>

        <div className="px-6 py-2 font-bold text-lg">
          <div className="flex gap-2">
            <Badge
              variant="outline"
              className={cn('rounded-sm text-black border-gray-400 bg-white')}
            >
              서울
            </Badge>
            <Badge
              variant="outline"
              className={cn('rounded-sm text-black border-gray-400 bg-white')}
            >
              10K
            </Badge>
          </div>
        </div>
        {/* 본문: 사용자 정보 */}
        <div className="px-6 py-4 space-y-4 ">
          <div className="p-3 grid grid-cols-3 gap-2 bg-gray-100 rounded-sm">
            <InfoLabel>일시</InfoLabel>
            <InfoValue>{data.eventDate}</InfoValue>
            <InfoLabel>집합 장소</InfoLabel>
            <InfoValue>{data.location}</InfoValue>
          </div>
        </div>

        <div className="px-6 py-2 font-bold">코스맵</div>

        <div className="px-6 py-2">
          <div className="relative w-full h-[150px] rounded-2xl overflow-hidden bg-gray-200 mb-2 border-2 rounded-none">
            <div className="w-full h-full flex items-center justify-center text-gray-400 ">
              코스 지도 영역
            </div>
          </div>
        </div>

        {/* 하단 버튼 */}
        <div
          className={cn(
            'flex gap-2 px-6 py-4',
            template === 0 ? 'flex-row' : 'flex-col',
          )}
        >
          <Button variant="outline" rounded="sm" onClick={onClose}>
            닫기
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
