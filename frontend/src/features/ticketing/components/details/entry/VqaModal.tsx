import { Button } from '@/components/ui/button';
import { RadioGroupItem } from '@/components/ui/radioGroup';
import { cn } from '@/lib/utils';
import { RadioGroup } from '@radix-ui/react-radio-group';
import React from 'react';

interface EventData {
  course: string;
  pace: string;
}

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

export const VqaModal = ({ isOpen, onClose, onConfirm }: ModalProps) => {
  if (!isOpen) return null;

  return (
    // 전체 화면 레이어 (z-index 50)
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-hidden">
      {/* 배경 레이어: 회색 투명 배경 + 블러 (클릭 이벤트 없음) */}
      <div className="absolute inset-0 bg-black/50 transition-opacity" />

      {/* 모달 본체 */}
      <div className="relative w-full max-w-md transform overflow-hidden rounded-2xl bg-white shadow-2xl transition-all animate-in fade-in zoom-in duration-300">
        {/* 헤더 부분: 제목과 X 버튼 */}
        <div className="relative flex items-center justify-between border-b border-gray-100 px-6 py-4">
          <h3 className="text-lg font-bold text-gray-900">보안 인증</h3>

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

        <div className="flex flex-col items-start px-6 py-4 w-full">
          {/* 비디오 섹션: 왼쪽 정렬 및 하단 여백 */}
          <div className="w-full mb-6">
            <video
              width="640"
              height="360"
              controls
              preload="metadata"
              className="rounded-lg shadow-sm"
            >
              <source src="/animations/vqa.webm" type="video/webm" />
              브라우저가 비디오 태그를 지원하지 않습니다.
            </video>
          </div>

          {/* 질문 섹션: 폰트 강조 및 간격 조정 */}
          <div className="text-lg font-bold mb-4">
            3등으로 결승선을 통과한 러너의 기록은 무엇입니까?
          </div>

          {/* 선택지 섹션: 라디오 그룹 정렬 */}
          <div className="w-full">
            <RadioGroup
              // value={selectedValue}
              // onValueChange={setSelectedValue}
              className="flex flex-col"
            >
              {[
                { id: '1', label: '02:15:20.05' },
                { id: '2', label: '02:15:20.05' },
                { id: '3', label: '02:15:20.05' },
                { id: '4', label: '02:15:20.05' },
              ].map((item) => (
                <label
                  key={item.id}
                  htmlFor={item.id}
                  className="flex items-center space-x-3 cursor-pointer p-2 rounded-md transition-colors"
                >
                  <RadioGroupItem value={item.id} id={item.id} />
                  <span className="text-base font-medium">{item.label}</span>
                </label>
              ))}
            </RadioGroup>
          </div>
        </div>

        {/* 하단 버튼 */}
        <div className={cn('flex gap-2 px-6 py-4')}>
          <Button variant="primary1" rounded="full" onClick={onConfirm}>
            제출하기
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
