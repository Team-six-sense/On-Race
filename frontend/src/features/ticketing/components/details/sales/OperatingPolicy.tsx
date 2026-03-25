'use client';

import { useEffect, useState } from 'react';
import { LuChevronDown, LuChevronUp } from 'react-icons/lu';

export function OperatingPolicy({
  isOpen,
  onToggle,
}: {
  isOpen: boolean;
  onToggle: () => void;
}) {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <section className="border-b border-gray-300">
      {/* 클릭 가능한 헤더 영역 */}
      <button
        onClick={onToggle}
        className="w-full flex justify-between items-center py-5 px-2 focus:outline-none"
      >
        <h2 className="text-xl font-bold text-black">운영 정책</h2>
        {/* 상태에 따른 아이콘 변경 */}
        {isOpen ? <LuChevronUp size={24} /> : <LuChevronDown size={24} />}
      </button>

      {/* 펼쳐지는 내용 영역 */}
      <div
        className={`grid transition-all duration-300 ease-in-out ${
          isOpen
            ? 'grid-rows-[1fr] opacity-100 mb-6'
            : 'grid-rows-[0fr] opacity-0 mb-0'
        }`}
      >
        <div className="overflow-hidden">
          <div className="text-sm">
            <ul className="space-y-1.5">
              <li className="flex gap-1.5">
                <span>•</span>
                <span>
                  동일한 본인인증 정보로 중복 응모 시 1건만 유효 처리됩니다.
                </span>
              </li>
              <li className="flex gap-1.5">
                <span>•</span>
                <span>
                  단시간 과도한 요청이 감지될 경우 자동으로 접근이 제한될 수
                  있습니다.
                </span>
              </li>
              <li className="flex gap-1.5">
                <span>•</span>
                <span>
                  부정 이용이 의심되는 경우 관리자 검토 후 응모가 무효 처리될 수
                  있습니다.
                </span>
              </li>
              <li className="flex gap-1.5">
                <span>•</span>
                <span>
                  신청 후 10분 이내 결제를 완료하지 않으면 신청이 자동
                  취소됩니다.
                </span>
              </li>
              <li className="flex gap-1.5">
                <span>•</span>
                <span>
                  취소 발생 시 대기열 순서에 따라 자동으로 신청 기회가
                  부여됩니다.
                </span>
              </li>
              <li className="flex gap-1.5">
                <span>•</span>
                <span>
                  동시 접속 인원 초과 시 대기열로 이동하며, 순서대로 입장됩니다.
                </span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
}
